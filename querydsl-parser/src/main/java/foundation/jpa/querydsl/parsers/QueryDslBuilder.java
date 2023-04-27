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

package foundation.jpa.querydsl.parsers;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import foundation.jpa.querydsl.Context;
import foundation.jpa.querydsl.QueryUtils;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static com.querydsl.core.types.dsl.Expressions.*;
import static java.util.stream.IntStream.range;

public class QueryDslBuilder {

    private final Context context;
    private final EntityPath<?> rootEntity;

    public QueryDslBuilder(Context context, EntityPath<?> rootEntity) {
        this.context = context;
        this.rootEntity = rootEntity;
    }

    public OrderSpecifier<?> order(Order order, Expression<?> expression) {
        return new OrderSpecifier(order, expression);
    }

    public BooleanExpression ensureBoolean(Expression<?> e) {
        if(e instanceof BooleanExpression) return (BooleanExpression) e;
        throw new ClassCastException(e + " is not boolean.");
    }

    private void ensure(Class<? extends Expression> c, Expression<?>...ts) {
        for(Expression<?> t : ts) if(!c.isInstance(t))
            throw new IllegalArgumentException(t + " is not " + c.getSimpleName() + " but " + c.getSimpleName() + " is expected.");
    }

    private void ensureType(Class<?> c, Expression<?>...ts) {
        for(Expression<?> t : ts) if(!c.isAssignableFrom(t.getType()))
            throw new IllegalArgumentException(t + " is not " + c.getSimpleName() + " but " + c.getSimpleName() + " is expected.");
    }

    public BooleanExpression logical(Ops op, Expression<?> l, Expression<?> r) {
        ensure(BooleanExpression.class, l, r);
        return booleanOperation(op, l, r);
    }

    public BooleanExpression simple(Ops op, Expression<?> l, Expression<?> r) {
        return QueryUtils.resolveOperation(op, l, r, context);
    }

    public Expression<?> simpleOp(Ops op, Expression<?>... l) {
        return simpleOperation(l[l.length - 1].getType(), op, l);
    }

    public Expression<?> relational(Ops op, Expression<?> l, Expression<?> r) {
        ensureType(Comparable.class, l, r);
        return booleanOperation(op, l, r);
    }

    public <T extends Number & Comparable<T>> Expression<?> numerical(Ops op, Expression<?> l, Expression<?> r) {
        ensureType(Number.class, l, r);
        ensureType(Comparable.class, l, r);
        return numberOperation((Class<T>) l.getType(), op, l, r);
    }

    public Expression<?> resolve(String name) {
        return context.isDefined(name) ? value(context.get(name)) : access(rootEntity, name);
    }

    public Expression<?> call(String name, List<Expression<?>> parameters) {
        return call(rootEntity, name, parameters);
    }

    public Expression<?> value(Object value) {
        return constant(value);
    }

    private Expression<?> auto(Object value) {
        return value instanceof Expression ? (Expression<?>) value : constant(value);
    }

    private Object convert(Object object, Class<?> requiredClass) {
        if(requiredClass.isInstance(object))
            return object;
        if(requiredClass.isAssignableFrom(Expression.class))
            return constant(object);
        return
            context.convert(object, requiredClass);
    }

    private static boolean isCandidate(Method method, String name, int size) {
        return method.getName().equals(name) && (method.isVarArgs() ? size >= method.getParameterCount() - 1 : size == method.getParameterCount());
    }

    private static boolean matchParameterTypes(boolean isVarArgs, Class<?>[] parameterTypes, Object[] arguments) {
        Class<?> lastType = parameterTypes[parameterTypes.length - 1];
        if(isVarArgs && (arguments.length != parameterTypes.length || lastType.isInstance(arguments[arguments.length - 1]))) {
            Class<?> varArgType = lastType.getComponentType();
            return range(0, parameterTypes.length - 1).mapToObj(i -> parameterTypes[i].isInstance(arguments[i])).allMatch(m -> m)
                    && range(parameterTypes.length - 1, arguments.length).allMatch(i -> varArgType.isInstance(arguments[i]));
        } else {
            return range(0, parameterTypes.length).mapToObj(i -> parameterTypes[i].isInstance(arguments[i])).allMatch(m -> m);
        }
    }

    private Object[] args(boolean isVarArgs, Class<?>[] parameterTypes, Object[] arguments) {
        Object[] result = new Object[parameterTypes.length];
        int lastIndex = parameterTypes.length - 1;
        Class<?> lastType = parameterTypes[lastIndex];
        range(0, lastIndex).forEach(i -> result[i] = convert(arguments[i], parameterTypes[i]));
        if(isVarArgs && (arguments.length != parameterTypes.length || lastType.isInstance(arguments[arguments.length - 1]))) {
            Class<?> varArgType = lastType.getComponentType();
            Object vars = Array.newInstance(varArgType, arguments.length - lastIndex);
            range(lastIndex, arguments.length).forEach(i -> Array.set(vars, i, convert(arguments[i], varArgType)));
            result[result.length - 1] = vars;
        } else {
            result[lastIndex] = arguments[lastIndex];
        }
        return result;
    }

    public Expression<?> call(Expression<?> target, String name, List<Expression<?>> parameters) {
        Class<?> type = target.getClass();
        int size = parameters.size();
        Object[] arguments = parameters.stream().map(p -> p instanceof Constant ? ((Constant<?>) p).getConstant() : p).toArray();
        Deque<Method> candidates = new LinkedList<>();
        Stream.of(type.getMethods()).filter(method -> isCandidate(method, name, size)).forEach(method -> {
            Class<?>[] parameterTypes = method.getParameterTypes();
            boolean isVarArgs = method.isVarArgs();
            if(matchParameterTypes(isVarArgs, parameterTypes, arguments)) {
                candidates.addFirst(method);
            } else {
                candidates.addLast(method);
            }
        });
        if(candidates.isEmpty()) {
            throw new IllegalArgumentException("No such method: " + name + " on " + target + " with " + size + " parameters.");
        }
        try {
            Method method = candidates.getFirst();
            Class<?>[] parameterTypes = method.getParameterTypes();
            boolean isVarArgs = method.isVarArgs();
            Object[] a = args(isVarArgs, parameterTypes, arguments);
            return auto(method.invoke(target, a));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Unable to invoke method: " + name + " with " + Arrays.toString(arguments) + " on " + target + ".", e);
        }
    }

    public Expression<?> access(Expression<?> target, String name) {
        Object object = target instanceof Constant ? ((Constant<?>) target).getConstant() : target;
        Class<?> c = object instanceof Class ? (Class<?>) object : object.getClass();
        return auto(context.access(c, target, name));
    }

    public SimpleExpression<?> simple(Expression<?> operand) {
        ensure(SimpleExpression.class, operand);
        return asSimple(operand);
    }

    public BooleanExpression stringual(Ops op, Expression<?> l, Expression<?> r) {
        ensureType(String.class, l, r);
        return booleanOperation(op, l, r);
    }

    public BooleanExpression in(Ops op, Expression<?> l, List<Expression<?>> rs) {
        return rs.size() == 1 ? simple(Ops.EQ, l, rs.get(0)) : simple(op, l, Expressions.set(l instanceof EntityPath
                ? rs.stream().map(r -> r instanceof Constant ? QueryUtils.convert(r, l, context) : r).toArray(Expression[]::new)
                : rs.toArray(new Expression<?>[0])));
    }

    public <T extends Number & Comparable<T>> Expression<?> numerical(Operator negate, Expression<?> operand) {
        ensureType(Number.class, operand);
        ensureType(Comparable.class, operand);
        return numberOperation((Class<T>) operand.getType(), negate, operand);
    }

    public Expression<?> negation(Expression<?> e) {
        ensure(BooleanExpression.class, e);
        return booleanOperation(Ops.NOT, e);
    }

    public <T> Expression<T> alias(Expression<T> expression, String alias) {
        return new AliasExpression<>(Expressions.as(expression, context.define(alias, ExpressionUtils.path(expression.getType(), alias))), alias);
    }

}
