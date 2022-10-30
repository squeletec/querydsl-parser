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
import com.querydsl.core.types.dsl.StringExpression;
import foundation.jpa.querydsl.EntityConverter;
import foundation.jpa.querydsl.QueryUtils;
import foundation.jpa.querydsl.QueryVariables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static com.querydsl.core.types.dsl.Expressions.*;

public class QueryDslBuilder {

    private final SymbolTable context;
    private final EntityConverter entityConverter;
    private final EntityPath<?> rootEntity;

    public QueryDslBuilder(SymbolTable context, EntityConverter entityConverter, EntityPath<?> rootEntity) {
        this.context = context;
        this.entityConverter = entityConverter;
        this.rootEntity = rootEntity;
    }

    public OrderSpecifier<?> order(Order order, Expression<?> expression) {
        return new OrderSpecifier(order, expression);
    }

    public BooleanExpression ensureBoolean(Expression<?> e) {
        if(e instanceof BooleanExpression) return (BooleanExpression) e;
        throw new ClassCastException("" + e + " is not boolean.");
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

    public Entry resolve(String name) {
        return context.get(name);
    }

    public Expression<?> call(Entry entry, List<Expression<?>> parameters) {
        return entry.call(parameters, entityConverter);
    }

    public Expression<?> value(Object value) {
        return constant(value);
    }

    private Expression<?> auto(Object value) {
        return value instanceof Expression ? (Expression<?>) value : constant(value);
    }


    public Entry access(Expression<?> target, String name) {
        Object object = target instanceof Constant ? ((Constant<?>) target).getConstant() : target;
        Class<?> c = object instanceof Class ? (Class<?>) object : object.getClass();
        return context.access(c, target, name);
    }

    public SimpleExpression<?> simple(Expression<?> operand) {
        ensure(SimpleExpression.class, operand);
        return asSimple(operand);
    }

    public BooleanExpression stringual(Ops op, Expression<?> l, Expression<?> r) {
        ensure(StringExpression.class, l, r);
        return booleanOperation(op, l, r);
    }

    public BooleanExpression in(Ops op, Expression<?> l, List<Expression<?>> rs) {
        return rs.size() == 1 ? simple(Ops.EQ, l, rs.get(0)) : simple(op, l, Expressions.set(rs.toArray(new Expression<?>[0])));
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

}
