package foundation.jpa.querydsl;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import foundation.rpg.Match;
import foundation.rpg.StartSymbol;
import foundation.rpg.common.precedence.*;
import foundation.rpg.common.rules.CommaSeparated;
import foundation.rpg.common.symbols.*;

import java.lang.Class;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.querydsl.core.types.dsl.Expressions.constant;
import static foundation.jpa.querydsl.QueryUtils.operation;
import static foundation.jpa.querydsl.QueryUtils.resolve;
import static foundation.rpg.common.Patterns.*;
import static java.util.stream.Collectors.joining;

@SuppressWarnings({"unused", "unchecked"})
public class QueryFactory {

    private final EntityConverter entityConverter;
    private final Map<String, Object> variables;
    private final EntityPath<?> root;

    public QueryFactory(EntityConverter entityConverter, Map<String, Object> variables, EntityPath<?> root) {
        this.entityConverter = entityConverter;
        this.variables = variables;
        this.root = root;
    }

    @StartSymbol
    Predicate is (@LogicalOr BooleanExpression expression) {
        return expression;
    }

    @LogicalOr BooleanExpression is (@LogicalOr BooleanExpression leftOperand, Or operator, @LogicalAnd BooleanExpression rightOperand) {
        return leftOperand.or(rightOperand);
    }

    @LogicalAnd BooleanExpression is (@LogicalAnd BooleanExpression leftOperand, And operator, @Relational BooleanExpression rightOperand) {
        return leftOperand.and(rightOperand);
    }

    @Relational BooleanExpression is (Not operator, LPar opening, @LogicalOr BooleanExpression expression, RPar closing) {
        return expression.not();
    }

    @Relational BooleanExpression is (LPar opening, @LogicalOr BooleanExpression expression, RPar closing) {
        return expression;
    }

    @Relational BooleanExpression is (Expression leftOperand, Equal operator, Expression rightOperand) {
        if(leftOperand instanceof EntityPath && rightOperand instanceof Constant)
            return resolve(Ops.EQ, leftOperand, rightOperand, entityConverter);
        if(rightOperand instanceof EntityPath && leftOperand instanceof Constant)
            return resolve(Ops.EQ, rightOperand, leftOperand, entityConverter);
        return operation(Ops.EQ, leftOperand, rightOperand);
    }

    @Relational BooleanExpression is (Expression leftOperand, ExclEqual operator, Expression rightOperand) {
        if(leftOperand instanceof EntityPath && rightOperand instanceof Constant)
            return resolve(Ops.NE, leftOperand, rightOperand, entityConverter);
        if(rightOperand instanceof EntityPath && leftOperand instanceof Constant)
            return resolve(Ops.NE, rightOperand, leftOperand, entityConverter);
        return operation(Ops.NE, leftOperand, rightOperand);
    }

    @Relational BooleanExpression is (Expression operand, Is operator, Null nul) {
        return operation(Ops.IS_NULL, operand);
    }

    @Relational BooleanExpression is (Expression operand, Not operator, Null nul) {
        return operation(Ops.IS_NOT_NULL, operand);
    }

    @Relational BooleanExpression is (Expression leftOperand, In operator, LPar opening, @CommaSeparated List<Object> rightOperands, RPar closing) {
        return Expressions.asSimple(leftOperand).in(rightOperands);
    }

    @Relational BooleanExpression is (Expression leftOperand, Not negation, In operator, LPar opening, @CommaSeparated List<Object> rightOperands, RPar closing) {
        return Expressions.asSimple(leftOperand).notIn(rightOperands);
    }

    @Relational BooleanExpression is (Expression leftOperand, Tilda operator, Expression rightOperand) {
        return operation(Ops.LIKE, leftOperand, rightOperand);
    }

    @Relational BooleanExpression is(Expression leftOperand, Gt operator, Expression rightOperand) {
        return operation(Ops.GT, leftOperand, rightOperand);
    }

    @Relational BooleanExpression is(Expression leftOperand, Lt operator, Expression rightOperand) {
        return operation(Ops.LT, leftOperand, rightOperand);
    }

    @Relational BooleanExpression is(Expression leftOperand, GtEqual operator, Expression rightOperand) {
        return operation(Ops.GOE, leftOperand, rightOperand);
    }

    @Relational BooleanExpression is(Expression leftOperand, LtEqual operator, Expression rightOperand) {
        return operation(Ops.LOE, leftOperand, rightOperand);
    }

    @Relational BooleanExpression is (Expression expression) {
        return Expressions.asBoolean(expression);
    }

    Expression is1 (@Additive Expression expression) {
        return expression;
    }

    @Additive Expression is (@Additive Expression leftOperand, Plus operator, @Multiplicative Expression rightOperand) {
        return operation(Ops.ADD, leftOperand, rightOperand);
    }

    @Additive Expression is (@Additive Expression leftOperand, Minus operator, @Multiplicative Expression rightOperand) {
        return operation(Ops.SUB, leftOperand, rightOperand);
    }

    @Multiplicative Expression is (@Multiplicative Expression leftOperand, Star operator, @Unary Expression rightOperand) {
        return operation(Ops.MULT, leftOperand, rightOperand);
    }

    @Multiplicative Expression is (@Multiplicative Expression leftOperand, Sl operator, @Unary Expression rightOperand) {
        return operation(Ops.DIV, leftOperand, rightOperand);
    }

    @Unary Expression is(Minus operator, @Unary Expression operand) {
        return operation(Ops.NEGATE, operand);
    }

    @Unary Expression is2 (@Atomic Expression expression) {
        return expression;
    }

    Object is (@Match(DOUBLE) Double value) {
        return constant(value);
    }

    Object is (@Match(INTEGER) Integer value) {
        return constant(value);
    }

    Object is (@Match(QUOTED_STRING) String value) {
        return constant(value.substring(1, value.length() - 1));
    }

    Object is (Identifier identifier) {
        String id = identifier.toString();
        return id.equals(root.toString()) ? root : variables.containsKey(id) ? variables.get(id) : is(root, null, identifier);
    }


    Object is (Identifier identifier, LPar opening, @CommaSeparated List<Expression> parameters, RPar closing) {
        return is(root, null, identifier, opening, parameters, closing);
    }

    @Atomic Expression is (Object object) {
        return object instanceof Expression ? (Expression) object : constant(object);
    }

    Object is (Object object, Dot operator, Identifier property) {
        String name = property.toString();
        Class<?> type = object instanceof Class ? (Class<?>) object : object.getClass();
        try {
            return type.getField(name).get(object);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            try {
                return type.getMethod(name).invoke(object);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                throw new IllegalArgumentException("No such field: " + name + " on entity " + object + ". Available fields are: " + Stream.of(object.getClass().getFields()).map(Field::getName).collect(joining(", ")), ex);
            }
        }
    }

    Object is (Object object, Dot operator, Identifier property, LPar opening, @CommaSeparated List<Expression> parameters, RPar closing) {
        String name = property.toString();
        Class<?> type = object instanceof Class ? (Class<?>) object : object.getClass();
        int size = parameters.size();
        Object[] arguments = parameters.stream().map(p -> p instanceof Constant ? ((Constant<?>) p).getConstant() : p).toArray();
        for(Method method : type.getMethods()) try {
            if(method.getName().equals(name) && method.getParameterCount() == size) {
                Object[] a = IntStream.range(0, arguments.length).mapToObj(i -> entityConverter.convert(arguments[i], method.getParameterTypes()[i])).toArray();
                return method.invoke(object, a);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Unable to invoke method: " + property.toString() + " with " + Arrays.toString(arguments) + " on entity " + object + ".", e);
        }
        throw new IllegalArgumentException("No such method: " + property.toString() + " on entity " + object + " with " + size + " parameters.");
    }


    void ignore(WhiteSpace whiteSpace) {}

}
