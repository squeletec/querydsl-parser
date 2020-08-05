package foundation.jpa.querydsl;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.ListPath;
import foundation.rpg.Match;
import foundation.rpg.StartSymbol;
import foundation.rpg.common.precedence.LogicalAnd;
import foundation.rpg.common.precedence.LogicalOr;
import foundation.rpg.common.precedence.Relational;
import foundation.rpg.common.rules.CommaSeparated;
import foundation.rpg.common.symbols.*;
import org.springframework.core.convert.ConversionService;

import java.lang.Class;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.querydsl.core.types.dsl.Expressions.constant;
import static foundation.jpa.querydsl.QueryUtils.operation;
import static foundation.jpa.querydsl.QueryUtils.resolve;
import static foundation.rpg.common.Patterns.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings({"unused", "unchecked"})
public class QueryFactory {

    private final ConversionService conversionService;
    private final Map<String, Object> variables;
    private final EntityPath<?> root;

    public QueryFactory(ConversionService conversionService, Map<String, Object> variables, EntityPath<?> root) {
        this.conversionService = conversionService;
        this.variables = variables.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> constant(e.getValue())));
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
            return resolve(leftOperand, rightOperand, conversionService);
        if(rightOperand instanceof EntityPath && leftOperand instanceof Constant)
            return resolve(rightOperand, leftOperand, conversionService);
        return operation(Ops.EQ, leftOperand, rightOperand);
    }

    @Relational BooleanExpression is (Expression leftOperand, In operator, LPar opening, @CommaSeparated List<Expression> rightOperands, RPar closing) {
        return Expressions.asSimple(leftOperand).in(rightOperands);
    }

    @Relational BooleanExpression is (Expression leftOperand, Tilda operator, Expression rightOperand) {
        return operation(Ops.LIKE, leftOperand, rightOperand);
    }

    @Relational BooleanExpression is (Expression expression) {
        return Expressions.asBoolean(expression);
    }

    Expression is (@Match(DOUBLE) Double value) {
        return constant(value);
    }

    Expression is (@Match(INTEGER) Integer value) {
        return constant(value);
    }

    Expression is (@Match(QUOTED_STRING) String value) {
        return constant(value.substring(1, value.length() - 1));
    }

    Object is (Identifier identifier) {
        String id = identifier.toString();
        return id.equals(root.toString()) ? root : variables.containsKey(id) ? variables.get(id) : is(root, null, identifier);
    }


    Object is (Identifier identifier, LPar opening, @CommaSeparated List<Expression> parameters, RPar closing) {
        return is(root, null, identifier, opening, parameters, closing);
    }

    Expression is (Object object) {
        return object instanceof Expression ? (Expression) object : constant(object);
    }

    Object is (Object object, Dot operator, Identifier property) {
        String name = property.toString();
        if(object instanceof ListPath)
            return is(((ListPath<?, ?>) object).any(), operator, property);
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
                return method.invoke(object, arguments);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Unable to invoke method: " + property.toString() + " with " + Arrays.toString(arguments) + " on entity " + object + ".", e);
        }
        throw new IllegalArgumentException("No such method: " + property.toString() + " on entity " + object + " with " + size + " parameters.");
    }


    void ignore(WhiteSpace whiteSpace) {}

}
