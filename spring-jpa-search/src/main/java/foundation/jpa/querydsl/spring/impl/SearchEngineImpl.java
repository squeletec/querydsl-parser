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

package foundation.jpa.querydsl.spring.impl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import foundation.jpa.querydsl.QueryContext;
import foundation.jpa.querydsl.QueryVariables;
import foundation.jpa.querydsl.spring.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.List;

public class SearchEngineImpl implements SearchEngine {

    private final JPAQueryFactory queryFactory;
    private final QueryContext queryContext;

    public SearchEngineImpl(JPAQueryFactory queryFactory, QueryContext queryContext) {
        this.queryFactory = queryFactory;
        this.queryContext = queryContext;
    }

    public SearchEngineImpl(EntityManager entityManager) {
        this(new JPAQueryFactory(entityManager), new JpaQueryContext(entityManager));
    }

    @Override
    public <E> SearchResult<E> search(SearchCriteria<? extends EntityPath<E>> criteria, QueryVariables variables) {
        return search(criteria, variables, queryFactory.selectFrom(criteria.getEntityPath()));
    }

    @Override
    public <E> SearchResult<E> search(Predicate implicitPredicate, SearchCriteria<? extends EntityPath<E>> criteria, QueryVariables variables) {
        return search(criteria, variables, queryFactory.selectFrom(criteria.getEntityPath()).where(implicitPredicate));
    }

    @Override
    public <E> SearchResult<E> search(EntityPath<E> entityPath, String query, String sort, Pageable pageable, QueryVariables variables) {
        return search(new SearchCriteriaImpl<>("query", query, sort, pageable, entityPath), variables);
    }

    @Override
    public <E> SearchResult<List<?>> aggregate(AggregateCriteria<? extends EntityPath<E>> criteria, QueryVariables variables) {
        try {
            JPAQuery<List<?>> query = queryFactory.selectFrom(criteria.getEntityPath())
                    .where(queryContext.parsePredicate(criteria.getEntityPath(), criteria.getQuery(), variables))
                    .orderBy(queryContext.parseOrderSpecifier(criteria.getEntityPath(), criteria.getSort()))
                    .groupBy(queryContext.parseSelect(criteria.getEntityPath(), criteria.groupBy(), variables))
                    .select(Projections.list(queryContext.parseSelect(criteria.getEntityPath(), criteria.select(), variables)))
                    .offset(criteria.getPageable().getOffset())
                    .limit(criteria.getPageable().getPageSize());
            return new AggregationResultImpl(new PageImpl<>(query.fetch(), criteria.getPageable(), query.fetchCount()), null);
        } catch (Throwable e) {
            return new AggregationResultImpl(Page.empty(), e);
        }
    }

    private <E> SearchResult<E> search(SearchCriteria<? extends EntityPath<E>> criteria, QueryVariables variables, JPAQuery<E> jpaQuery) {
        try {
            JPAQuery<E> query = jpaQuery.where(queryContext.parsePredicate(criteria.getEntityPath(), criteria.getQuery(), variables))
                    .orderBy(queryContext.parseOrderSpecifier(criteria.getEntityPath(), criteria.getSort()))
                    .offset(criteria.getPageable().getOffset())
                    .limit(criteria.getPageable().getPageSize());
            return new SearchResultImpl<>(criteria, new PageImpl<>(query.fetch(), criteria.getPageable(), query.fetchCount()), null);
        } catch (Throwable e) {
            return new SearchResultImpl<>(criteria, Page.empty(), e);
        }
    }
}
