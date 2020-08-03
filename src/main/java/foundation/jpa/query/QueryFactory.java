package foundation.jpa.query;

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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.querydsl.core.types.dsl.Expressions.constant;
import static foundation.rpg.common.Patterns.*;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings({"unused", "unchecked"})
public class QueryFactory {

    private final EntityConstantResolver entityResolver;
    private final Map<String, Expression> variables;
    private final Map<String, Path> paths;
    private final EntityPath root;

    public QueryFactory(EntityConstantResolver entityResolver, Map<String, Object> variables, EntityPath root) {
        this.entityResolver = entityResolver;
        this.variables = variables.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> constant(e.getValue())));
        this.root = root;
        paths = Arrays.stream(root.getClass().getFields()).filter(f -> isPublic(f.getModifiers()) && Path.class.isAssignableFrom(f.getType())).collect(toMap(Field::getName, f -> {
            try {
                return (Path) f.get(root);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        }));
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
            return resolve(leftOperand, rightOperand);
        if(rightOperand instanceof EntityPath && leftOperand instanceof Constant)
            return resolve(rightOperand, leftOperand);
        return Expressions.asSimple(leftOperand).eq(rightOperand);
    }

    private BooleanExpression resolve(Expression leftOperand, Expression rightOperand) {
        return Expressions.asSimple(leftOperand).eq(entityResolver.resolveEntity(leftOperand.getType(), ((Constant) rightOperand).getConstant()));
    }

    @Relational BooleanExpression is (Expression leftOperand, In operator, LPar opening, @CommaSeparated List<Expression> rightOperands, RPar closing) {
        return Expressions.asSimple(leftOperand).in(rightOperands);
    }

    @Relational BooleanExpression is (Expression leftOperand, Tilda operator, Expression rightOperand) {
        return Expressions.asString(leftOperand).like(rightOperand);
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

    Expression is (Identifier identifier) {
        if(paths.containsKey(identifier.toString()))
            return paths.get(identifier.toString());
        return variables.getOrDefault(identifier.toString(), constant(identifier.toString()));
    }

    Expression is (Path object) {
        return object;
    }

    Path is (Identifier target, Dot operator, Identifier property) {
        return is(is(root, operator, target), operator, property);
    }

    Path is (Path object, Dot operator, Identifier property) {
        try {
            if(object instanceof ListPath)
                return is((Path) ((ListPath) object).any(), operator, property);
            return (Path) object.getClass().getField(property.toString()).get(object);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException("No such field: " + property.toString() + " on entity " + object + ". Available fields are: " + Stream.of(object.getClass().getFields()).map(Field::getName).collect(joining(", ")));
        }
    }

    void ignore(WhiteSpace whiteSpace) {}

}
