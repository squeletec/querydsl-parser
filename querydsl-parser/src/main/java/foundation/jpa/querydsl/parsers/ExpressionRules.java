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
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.SimpleExpression;
import foundation.rpg.Match;
import foundation.rpg.Name;
import foundation.rpg.common.precedence.*;
import foundation.rpg.common.rules.CommaSeparated;
import foundation.rpg.common.symbols.*;
import foundation.rpg.parser.Token;

import java.util.List;
import java.util.function.UnaryOperator;

import static foundation.rpg.common.Patterns.*;

public interface ExpressionRules {

    //Expression<?> is(@Name("case") Token c, Expression<?> e, @Name("when") Token w, Expression<?> v, Then t, @Dangling @Name("otherwise") Token o, Expression<?> oe);
    //Expression<?> is(@LogicalOr Expression<?> e);

    Expression<?> is1(Case c, Expression<?> e, UnaryOperator<Expression<?>> w, Else o, Expression<?> oe);

    UnaryOperator<Expression<?>> is(When w, Expression<?> e, Then t, Expression<?> m);

    UnaryOperator<Expression<?>> is(UnaryOperator<Expression<?>> s, When w, Expression<?> e, Then t, Expression<?> m);
    Expression<?> isX(@LogicalOr Expression<?> e);

    @LogicalOr  Expression<?> is (@LogicalOr  Expression<?> l, Or op , @LogicalAnd Expression<?> r);
    @LogicalAnd Expression<?> is (@LogicalAnd Expression<?> l, And op, @Relational Expression<?> r);
    @LogicalAnd Expression<?> is(Not not, LPar o, Expression<?> e, RPar c);

    @Relational Expression<?> is (@Additive Expression<?> l, Equal operator, @Additive Expression<?> r);
    @Relational Expression<?> is (@Additive Expression<?> l, ExclEqual operator, @Additive Expression<?> r);
    @Relational Expression<?> is (@Additive Expression<?> operand, Is operator, Null nul);
    @Relational Expression<?> is (@Additive Expression<?> operand, Not operator, Null nul);
    @Relational Expression<?> is (@Additive Expression<?> l, In in, LPar o, @CommaSeparated List<@Additive Expression<?>> rs, RPar c);
    @Relational Expression<?> is (@Additive Expression<?> l, Not negation, In in, LPar o, @CommaSeparated List<@Additive Expression<?>> rs, RPar c);
    @Relational Expression<?> is (@Additive Expression<?> l, Tilda op, @Additive Expression<?> r);
    @Relational Expression<?> is (@Additive Expression<?> l, Gt op, @Additive Expression<?> r);
    @Relational Expression<?> is (@Additive Expression<?> l, Lt op, @Additive Expression<?> r);
    @Relational Expression<?> is (@Additive Expression<?> l, GtEqual op, @Additive Expression<?> r);
    @Relational Expression<?> is (@Additive Expression<?> l, LtEqual op, @Additive Expression<?> r);
    @Relational Expression<?> is1(@Additive Expression<?> o);

    @Additive Expression<?> is (@Additive Expression<?> l, Plus op, @Multiplicative Expression<?> r);
    @Additive Expression<?> is (@Additive Expression<?> l, Minus op, @Multiplicative Expression<?> r);

    @Multiplicative Expression<?> is (@Multiplicative Expression<?> l, Star op, @Unary Expression<?> r);
    @Multiplicative Expression<?> is (@Multiplicative Expression<?> l, Sl op, @Unary Expression<?> r);
    @Multiplicative Expression<?> is (@Multiplicative Expression<?> l, @Name("%") Token op, @Unary Expression<?> r);

    @Unary Expression<?> is(Minus op, @Unary Expression<?> operand);
    @Unary Expression<?> is3(@Atomic Expression<?> expression);

    @Atomic Expression<?> is (LPar l, Expression<?> expression, RPar r);
    @Atomic Expression<?> is (Identifier id);
    @Atomic Expression<?> is (@Name("count") Token count, LPar o, Expression<?> e, RPar c);
    @Atomic Expression<?> is1(@Name("sum") Token sum, LPar o, Expression<?> e, RPar c);
    @Atomic Expression<?> is2(@Name("avg") Token avg, LPar o, Expression<?> e, RPar c);
    @Atomic Expression<?> is3(@Name("min") Token count, LPar o, Expression<?> e, RPar c);
    @Atomic Expression<?> is4(@Name("max") Token count, LPar o, Expression<?> e, RPar c);
    @Atomic Expression<?> is (Identifier id, LPar o, @CommaSeparated List<Expression<?>> parameters, RPar r);
    @Atomic Expression<?> is (@Atomic Expression<?> target, Dot dot, Identifier id);
    @Atomic Expression<?> is (@Atomic Expression<?> target, Dot dot, Identifier id, LPar o, @CommaSeparated List<Expression<?>> parameters, RPar r);
    @Atomic Expression<?> is (Object value);

    Object is (@Match(DOUBLE) Double value);
    Object is (@Match(INTEGER) Long value);
    Object is (@Match(QUOTED_STRING) String value);
    Object is (True value);
    Object is (False value);

    @SuppressWarnings("unused")
    void ignore(WhiteSpace whiteSpace);

}
