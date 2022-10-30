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

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import foundation.rpg.Match;
import foundation.rpg.Name;
import foundation.rpg.common.precedence.*;
import foundation.rpg.common.rules.CommaSeparated;
import foundation.rpg.common.symbols.*;
import foundation.rpg.parser.Token;

import java.util.List;
import java.util.function.UnaryOperator;

import static com.querydsl.core.types.Ops.*;
import static com.querydsl.core.types.Ops.AggOps.*;
import static com.querydsl.core.types.Order.ASC;
import static com.querydsl.core.types.Order.DESC;
import static foundation.rpg.common.Patterns.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class QueryRulesImpl implements QueryRules, SelectRules, OrderRules {

    private final QueryDslBuilder builder;

    public QueryRulesImpl(QueryDslBuilder builder) {
        this.builder = builder;
    }

    @Override
    public OrderSpecifier<?>[] is1(@CommaSeparated List<OrderSpecifier<?>> l) {
        return l.toArray(new OrderSpecifier<?>[0]);
    }

    @Override
    public OrderSpecifier<?> is4(Expression<?> expression) {
        return builder.order(ASC, expression);
    }

    @Override
    public OrderSpecifier<?> is1(OrderSpecifier<?> specifier, @Name("nulls") Token nulls, @Name("first") Token last) {
        return specifier.nullsFirst();
    }

    @Override
    public OrderSpecifier<?> is2(OrderSpecifier<?> specifier, @Name("nulls") Token nulls, @Name("last") Token last) {
        return specifier.nullsLast();
    }

    @Override
    public OrderSpecifier<?> is1(Expression<?> expression, @Name("asc") Token asc) {
        return builder.order(ASC, expression);
    }

    @Override
    public OrderSpecifier<?> is2(Expression<?> expression, @Name("desc") Token desc) {
        return builder.order(DESC, expression);
    }

    @Override
    public Expression<?>[] is(@CommaSeparated List<Expression<?>> l) {
        return l.toArray(new Expression<?>[0]);
    }

    @Override
    public Predicate is0(Expression<?> e) {
        return builder.ensureBoolean(e);
    }

    @Override
    public Expression<?> is1(Case c, Expression<?> e, UnaryOperator<Expression<?>> w, Else o, Expression<?> oe) {
        return builder.simpleOp(CASE_EQ, e, w.apply(builder.simpleOp(CASE_ELSE, oe)));
    }

    @Override
    public UnaryOperator<Expression<?>> is(When w, Expression<?> e, Then t, Expression<?> m) {
        return next -> builder.simpleOp(CASE_EQ_WHEN, e, e, m, next);
    }

    @Override
    public UnaryOperator<Expression<?>> is(UnaryOperator<Expression<?>> f, When w, Expression<?> e, Then t, Expression<?> m) {
        return next -> f.apply(is(w, e, t, m).apply(next));
    }

    @Override
    public Expression<?> isX(@LogicalOr Expression<?> e) {
        return e;
    }

    @Override
    public @LogicalOr Expression<?> is(@LogicalOr Expression<?> l, Or op, @LogicalAnd Expression<?> r) {
        return builder.logical(OR, l, r);
    }

    @Override
    public @LogicalAnd Expression<?> is(@LogicalAnd Expression<?> l, And op, @Relational Expression<?> r) {
        return builder.logical(AND, l, r);
    }

    @Override
    public @LogicalAnd Expression<?> is(Not not, LPar o, Expression<?> e, RPar c) {
        return builder.negation(e);
    }

    @Override
    public @Relational Expression<?> is(@Additive Expression<?> l, Equal operator, @Additive Expression<?> r) {
        return builder.simple(EQ, l, r);
    }

    @Override
    public @Relational Expression<?> is(@Additive Expression<?> l, ExclEqual operator, @Additive Expression<?> r) {
        return builder.simple(NE, l, r);
    }

    @Override
    public @Relational Expression<?> is(@Additive Expression<?> operand, Is operator, Null nul) {
        return builder.simple(operand).isNull();
    }

    @Override
    public @Relational Expression<?> is(@Additive Expression<?> operand, Not operator, Null nul) {
        return builder.simple(operand).isNotNull();
    }

    @Override
    public @Relational Expression<?> is(@Additive Expression<?> l, In in, LPar o, @CommaSeparated List<@Additive Expression<?>> rs, RPar c) {
        return builder.in(IN, l, rs);
    }

    @Override
    public @Relational Expression<?> is(@Additive Expression<?> l, Not not, In in, LPar o, @CommaSeparated List<@Additive Expression<?>> rs, RPar c) {
        return builder.in(NOT_IN, l, rs);
    }

    @Override
    public @Relational Expression<?> is(@Additive Expression<?> l, Tilda op, @Additive Expression<?> r) {
        return builder.stringual(LIKE, l, r);
    }

    @Override
    public @Relational Expression<?> is(@Additive Expression<?> l, Gt op, @Additive Expression<?> r) {
        return builder.relational(GT, l, r);
    }

    @Override
    public @Relational Expression<?> is(@Additive Expression<?> l, Lt op, @Additive Expression<?> r) {
        return builder.relational(LT, l, r);
    }

    @Override
    public @Relational Expression<?> is(@Additive Expression<?> l, GtEqual op, @Additive Expression<?> r) {
        return builder.relational(GOE, l, r);
    }

    @Override
    public @Relational Expression<?> is(@Additive Expression<?> l, LtEqual op, @Additive Expression<?> r) {
        return builder.relational(LOE, l, r);
    }

    @Override
    public @Relational Expression<?> is1(@Additive Expression<?> o) {
        return o;
    }

    @Override
    public @Additive Expression<?> is(@Additive Expression<?> l, Plus op, @Multiplicative Expression<?> r) {
        return builder.numerical(ADD, l, r);
    }

    @Override
    public @Additive Expression<?> is(@Additive Expression<?> l, Minus op, @Multiplicative Expression<?> r) {
        return builder.numerical(SUB, l, r);
    }

    @Override
    public @Multiplicative Expression<?> is(@Multiplicative Expression<?> l, Star op, @Unary Expression<?> r) {
        return builder.numerical(MULT, l, r);
    }

    @Override
    public @Multiplicative Expression<?> is(@Multiplicative Expression<?> l, Sl op, @Unary Expression<?> r) {
        return builder.numerical(DIV, l, r);
    }

    @Override
    public @Multiplicative Expression<?> is(@Multiplicative Expression<?> l, @Name("%") Token op, @Unary Expression<?> r) {
        return builder.numerical(MOD, l, r);
    }

    @Override
    public @Unary Expression<?> is(Minus op, @Unary Expression<?> operand) {
        return builder.numerical(Ops.NEGATE, operand);
    }

    @Override
    public @Unary Expression<?> is3(@Atomic Expression<?> expression) {
        return expression;
    }

    @Override
    public @Atomic Expression<?> is(LPar l, Expression<?> expression, RPar r) {
        return expression;
    }

    @Override
    public Entry is(Identifier id) {
        return builder.resolve(id.toString());
    }

    @Override
    public @Atomic Expression<?> is(@Name("count") Token count, LPar o, Expression<?> e, RPar c) {
        return builder.simple(e).count();
    }

    @Override
    public @Atomic Expression<?> is1(@Name("sum") Token sum, LPar o, Expression<?> e, RPar c) {
        return builder.numerical(SUM_AGG, e instanceof BooleanExpression
                ? is1((Case) null, e, is((When) null, is(true), (Then) null, is(1)) , (Else) null, is(0))
                : e);
    }

    @Override
    public @Atomic Expression<?> is2(@Name("avg") Token avg, LPar o, Expression<?> e, RPar c) {
        return builder.numerical(AVG_AGG, e);
    }

    @Override
    public @Atomic Expression<?> is3(@Name("min") Token count, LPar o, Expression<?> e, RPar c) {
        return builder.numerical(MIN_AGG, e);
    }

    @Override
    public @Atomic Expression<?> is4(@Name("max") Token count, LPar o, Expression<?> e, RPar c) {
        return builder.numerical(MAX_AGG, e);
    }

    @Override
    public @Atomic Expression<?> is(Entry entry) {
        return entry.get();
    }

    @Override
    public Entry is(@Atomic Expression<?> target, Dot dot, Identifier id) {
        return builder.access(target, id.toString());
    }

    @Override
    public @Atomic Expression<?> is(Entry entry, LPar o, @CommaSeparated List<Expression<?>> parameters, RPar r) {
        return builder.call(entry, parameters);
    }

    @Override
    public @Atomic Expression<?> is(Object value) {
        return builder.value(value);
    }

    @Override
    public Object is(@Match(DOUBLE) Double value) {
        return value;
    }

    @Override
    public Object is(@Match(INTEGER) Long value) {
        return value;
    }

    @Override
    public Object is(@Match(QUOTED_STRING) String value) {
        return value.substring(1, value.length() - 1).replace("\\r", "\r").replace("\\n", "\n").replace("\\t", "\t").replaceAll("\\\\(.)", "$1");
    }

    @Override
    public Object is(True value) {
        return TRUE;
    }

    @Override
    public Object is(False value) {
        return FALSE;
    }

    @Override
    public void ignore(WhiteSpace whiteSpace) {}

}
