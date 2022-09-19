/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020-2022, Ondrej Fischer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package foundation.jpa.querydsl.group;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.MapPath;
import foundation.jpa.querydsl.EntityConverter;
import foundation.jpa.querydsl.QueryVariables;
import foundation.rpg.Match;
import foundation.rpg.Name;
import foundation.rpg.StartSymbol;
import foundation.rpg.common.precedence.*;
import foundation.rpg.common.rules.CommaSeparated;
import foundation.rpg.common.rules.NonEmpty;
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
import static foundation.jpa.querydsl.QueryUtils.resolveOperation;
import static foundation.jpa.querydsl.QueryVariables.local;
import static foundation.rpg.common.Patterns.*;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.joining;

@SuppressWarnings({"unused", "unchecked"})
public class GroupFactory {

    private final EntityConverter entityConverter;
    private final QueryVariables variables;
    private final EntityPath<?> root;

    public GroupFactory(EntityConverter entityConverter, QueryVariables variables, EntityPath<?> root) {
        this.entityConverter = entityConverter;
        this.variables = local(singletonMap(root.toString(), root), variables);
        this.root = root;
    }

    @StartSymbol(parserClassName = "GroupParser", lexerClassName = "GroupLexer")
    Expression[] is () {
        return new Expression<?>[0];
    }

    Expression[] is(@NonEmpty List<Expression> groups) {
        return groups.toArray(new Expression[]{});
    }

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
        return value;
    }

    Object is (@Match(INTEGER) Long value) {
        return value;
    }

    Object is (@Match(QUOTED_STRING) String value) {
        return value.substring(1, value.length() - 1);
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
            if(object instanceof MapPath) {
                return ((MapPath) object).get(name);
            }
            try {
                return type.getMethod("get", String.class).invoke(object, name);
            } catch (IllegalAccessException | NoSuchMethodException eex) {
                try {
                    return type.getMethod(name).invoke(object);
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                    throw new IllegalArgumentException("No such field: " + name + " on entity " + object + ". Available fields are: " + Stream.of(object.getClass().getFields()).map(Field::getName).collect(joining(", ")), ex);
                }
            } catch (InvocationTargetException ex) {
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
            throw new IllegalArgumentException("Unable to invoke method: " + property + " with " + Arrays.toString(arguments) + " on entity " + object + ".", e);
        }
        throw new IllegalArgumentException("No such method: " + property + " on entity " + object + " with " + size + " parameters.");
    }


    void ignore(WhiteSpace whiteSpace) {}

}
