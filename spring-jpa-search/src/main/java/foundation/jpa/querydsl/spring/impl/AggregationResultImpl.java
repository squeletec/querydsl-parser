package foundation.jpa.querydsl.spring.impl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import foundation.jpa.querydsl.spring.SearchCriteria;
import foundation.jpa.querydsl.spring.SearchResult;
import org.springframework.data.domain.Page;

import static java.util.Objects.nonNull;

public class AggregationResultImpl implements SearchResult<Tuple> {

    private final Page<Tuple> page;
    private final Throwable error;

    public AggregationResultImpl(Page<Tuple> page, Throwable error) {
        this.page = page;
        this.error = error;
    }

    @Override
    public SearchCriteria<? extends EntityPath<Tuple>> getCriteria() {
        return null;
    }

    @Override
    public Page<Tuple> getPage() {
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

    @Override
    public String toString() {
        return page.toString();
    }

}
