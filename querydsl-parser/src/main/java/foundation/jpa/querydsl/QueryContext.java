/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020-2020, Ondrej Fischer
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
import foundation.jpa.querydsl.group.GroupFactory;
import foundation.jpa.querydsl.group.GroupParser;
import foundation.jpa.querydsl.groupby.GroupByFactory;
import foundation.jpa.querydsl.groupby.GroupByParser;
import foundation.jpa.querydsl.order.OrderByParser;
import foundation.jpa.querydsl.order.OrderFactory;
import foundation.jpa.querydsl.where.PredicateParser;
import foundation.jpa.querydsl.where.QueryFactory;
import foundation.rpg.parser.SyntaxError;

import java.io.IOException;

public class QueryContext {

    private final EntityConverter entityConverter;

    public QueryContext(EntityConverter entityConverter) {
        this.entityConverter = entityConverter;
    }

    public static QueryContext createContext(EntityConverter conversionService) {
        return new QueryContext(conversionService);
    }

    public static QueryContext createContext() {
        return new QueryContext(EntityConverter.noConversion());
    }

    public Predicate parsePredicate(EntityPath<?> entityPath, String query, QueryVariables queryVariables) throws IOException, SyntaxError {
        if(query == null || query.isEmpty())
            return new BooleanBuilder().and(null);
        return new PredicateParser(new QueryFactory(entityConverter, queryVariables, entityPath)).parseString(query);
    }

    public OrderSpecifier<?>[] parseOrderSpecifier(EntityPath<?> entityPath, String orderBy) throws IOException {
        if(orderBy == null)
            return new OrderSpecifier[0];
        return new OrderByParser(new OrderFactory(entityPath)).parseString(orderBy);
    }

    public Expression<?>[] parseSelect(EntityPath<?> entityPath, String select, QueryVariables queryVariables) throws IOException {
        if(select == null) {
            return new Expression<?>[0];
        }
        return new GroupParser(new GroupFactory(entityConverter, queryVariables, entityPath)).parseString(select);
    }

}
