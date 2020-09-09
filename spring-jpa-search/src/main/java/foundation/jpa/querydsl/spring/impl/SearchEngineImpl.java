package foundation.jpa.querydsl.spring.impl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import foundation.jpa.querydsl.QueryContext;
import foundation.jpa.querydsl.QueryVariables;
import foundation.jpa.querydsl.spring.JpaQueryContext;
import foundation.jpa.querydsl.spring.SearchCriteria;
import foundation.jpa.querydsl.spring.SearchEngine;
import foundation.jpa.querydsl.spring.SearchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;

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
