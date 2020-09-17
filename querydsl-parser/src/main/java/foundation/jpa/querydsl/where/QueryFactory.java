package foundation.jpa.querydsl.where;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import foundation.jpa.querydsl.EntityConverter;
import foundation.jpa.querydsl.QueryVariables;
import foundation.rpg.Match;
import foundation.rpg.Name;
import foundation.rpg.StartSymbol;
import foundation.rpg.common.precedence.*;
import foundation.rpg.common.rules.CommaSeparated;
import foundation.rpg.common.symbols.*;
import foundation.rpg.parser.Token;

import java.lang.Class;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.querydsl.core.types.dsl.Expressions.*;
import static foundation.jpa.querydsl.QueryUtils.*;
import static foundation.jpa.querydsl.QueryVariables.local;
import static foundation.rpg.common.Patterns.*;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.joining;

@SuppressWarnings({"unused", "unchecked"})
public class QueryFactory {

    private final EntityConverter entityConverter;
    private final QueryVariables variables;
    private final EntityPath<?> root;

    public QueryFactory(EntityConverter entityConverter, QueryVariables variables, EntityPath<?> root) {
        this.entityConverter = entityConverter;
        this.variables = local(singletonMap(root.toString(), root), variables);
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
        return resolveOperation(Ops.EQ, leftOperand, rightOperand, entityConverter);
    }

    @Relational BooleanExpression is (Expression leftOperand, ExclEqual operator, Expression rightOperand) {
        return resolveOperation(Ops.NE, leftOperand, rightOperand, entityConverter);
    }

    @Relational BooleanExpression is (Expression operand, Is operator, Null nul) {
        return asSimple(operand).isNull();
    }

    @Relational BooleanExpression is (Expression operand, Not operator, Null nul) {
        return asSimple(operand).isNotNull();
    }

    @Relational BooleanExpression is (Expression leftOperand, In operator, LPar opening, @CommaSeparated List<Object> rightOperands, RPar closing) {
        return asSimple(leftOperand).in(rightOperands);
    }

    @Relational BooleanExpression is (Expression leftOperand, Not negation, In operator, LPar opening, @CommaSeparated List<Object> rightOperands, RPar closing) {
        return asSimple(leftOperand).notIn(rightOperands);
    }

    @Relational BooleanExpression is (Expression leftOperand, Tilda operator, Expression rightOperand) {
        return asString(leftOperand).like(rightOperand);
    }

    @Relational BooleanExpression is(Expression leftOperand, Gt operator, Expression rightOperand) {
        return asComparable(leftOperand).gt(rightOperand);
    }

    @Relational BooleanExpression is(Expression leftOperand, Lt operator, Expression rightOperand) {
        return asComparable(leftOperand).lt(rightOperand);
    }

    @Relational BooleanExpression is(Expression leftOperand, GtEqual operator, Expression rightOperand) {
        return asComparable(leftOperand).goe(rightOperand);
    }

    @Relational BooleanExpression is(Expression leftOperand, LtEqual operator, Expression rightOperand) {
        return asComparable(leftOperand).loe(rightOperand);
    }

    @Relational BooleanExpression is (Expression expression) {
        return asBoolean(expression);
    }

    Expression is1 (@Additive Expression expression) {
        return expression;
    }

    @Additive Expression is (@Additive Expression leftOperand, Plus operator, @Multiplicative Expression rightOperand) {
        return asNumber(leftOperand).add(rightOperand);
    }

    @Additive Expression is (@Additive Expression leftOperand, Minus operator, @Multiplicative Expression rightOperand) {
        return asNumber(leftOperand).subtract(rightOperand);
    }

    @Multiplicative Expression is (@Multiplicative Expression leftOperand, Star operator, @Unary Expression rightOperand) {
        return asNumber(leftOperand).multiply(rightOperand);
    }

    @Multiplicative Expression is (@Multiplicative Expression leftOperand, Sl operator, @Unary Expression rightOperand) {
        return asNumber(leftOperand).divide(rightOperand);
    }

    @Multiplicative Expression is (@Multiplicative Expression leftOperand, @Name("%") Token operator, @Unary Expression rightOperand) {
        return asNumber(leftOperand).mod(rightOperand);
    }

    @Unary Expression is(Minus operator, @Unary Expression operand) {
        return asNumber(operand).negate();
    }

    @Unary Expression is2 (@Atomic Expression expression) {
        return expression;
    }

    @Atomic Expression is (Object object) {
        return object instanceof Expression ? (Expression) object : constant(object);
    }

    Object is (@Match(DOUBLE) Double value) {
        return constant(value);
    }

    Object is (@Match(INTEGER) Long value) {
        return constant(value);
    }

    Object is (@Match(QUOTED_STRING) String value) {
        return constant(value.substring(1, value.length() - 1));
    }

    Object is (Identifier identifier) {
        String id = identifier.toString();
        return variables.isDefined(id) ? variables.get(id) : is(root, null, identifier);
    }


    Object is (Identifier identifier, LPar opening, @CommaSeparated List<Expression> parameters, RPar closing) {
        return is(root, null, identifier, opening, parameters, closing);
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
