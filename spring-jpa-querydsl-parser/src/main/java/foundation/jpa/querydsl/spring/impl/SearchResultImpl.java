package foundation.jpa.querydsl.spring.impl;

import com.querydsl.core.types.EntityPath;
import foundation.jpa.querydsl.spring.Search;
import foundation.jpa.querydsl.spring.SearchCriteria;
import foundation.jpa.querydsl.spring.SearchResult;
import org.springframework.data.domain.Page;

import static java.util.Objects.nonNull;

public class SearchResultImpl<E, Q extends EntityPath<E>> implements SearchResult<E>, Search<Q, E> {

    private final SearchCriteria<? extends EntityPath<E>> criteria;
    private final Page<E> page;
    private final Throwable error;

    public SearchResultImpl(SearchCriteria<? extends EntityPath<E>> criteria, Page<E> page, Throwable error) {
        this.criteria = criteria;
        this.page = page;
        this.error = error;
    }

    @Override
    public SearchCriteria<? extends EntityPath<E>> getCriteria() {
        return criteria;
    }

    @Override
    public Page<E> getPage() {
        return page;
    }

    @Override
    public Throwable getError() {
        return error;
    }

    @Override
    public boolean hasError() {
        return nonNull(error);
    }

}
