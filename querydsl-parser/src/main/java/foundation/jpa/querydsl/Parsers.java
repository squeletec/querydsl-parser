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

package foundation.jpa.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import foundation.jpa.querydsl.parsers.QueryDslBuilder;
import foundation.jpa.querydsl.parsers.QueryRulesImpl;
import foundation.jpa.querydsl.parsers.expressions.ExpressionsParser;
import foundation.jpa.querydsl.parsers.order.OrderSpecifierParser;
import foundation.jpa.querydsl.parsers.predicate.PredicateParser;
import foundation.rpg.parser.SyntaxError;

import java.io.IOException;

public class Parsers<T> {

    private final PredicateParser predicateParser;
    private final OrderSpecifierParser orderSpecifierParser;
    private final ExpressionsParser expressionsParser;

    private Parsers(PredicateParser predicateParser, OrderSpecifierParser orderSpecifierParser, ExpressionsParser expressionsParser) {
        this.predicateParser = predicateParser;
        this.orderSpecifierParser = orderSpecifierParser;
        this.expressionsParser = expressionsParser;
    }

    public Parsers(EntityPath<T> entityPath, QueryVariables queryVariables, EntityConverter entityConverter) {
        this(new QueryRulesImpl(new QueryDslBuilder(Context.map(queryVariables, entityConverter), entityConverter, entityPath)));
    }

    private Parsers(QueryRulesImpl queryRules) {
        this(new PredicateParser(queryRules), new OrderSpecifierParser(queryRules), new ExpressionsParser(queryRules));
    }

    public Predicate parsePredicate(String query) throws IOException, SyntaxError {
        if(query == null || query.isEmpty())
            return new BooleanBuilder().and(null);
        return predicateParser.parseString(query);
    }

    public OrderSpecifier<?>[] parseOrderSpecifier(String orderBy) throws IOException {
        if(orderBy == null)
            return new OrderSpecifier[0];
        return orderSpecifierParser.parseString(orderBy);
    }

    public Expression<?>[] parseSelect(String select) throws IOException {
        if(select == null)
            return new Expression<?>[0];
        return expressionsParser.parseString(select);
    }

}
