package foundation.jpa.querydsl.spring.impl;

import com.querydsl.core.types.EntityPath;
import foundation.jpa.querydsl.spring.SearchCriteria;
import org.springframework.data.domain.Pageable;

public class SearchCriteriaImpl<Q extends EntityPath<?>> implements SearchCriteria<Q> {

    private final String parameterName;
    private final String query;
    private final String sort;
    private final Pageable pageable;
    private final Q entityPath;

    public SearchCriteriaImpl(String parameterName, String query, String sort, Pageable pageable, Q entityPath) {
        this.parameterName = parameterName;
        this.query = query;
        this.sort = sort;
        this.pageable = pageable;
        this.entityPath = entityPath;
    }

    @Override
    public String getParameterName() {
        return parameterName;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public String getSort() {
        return sort;
    }

    @Override
    public Pageable getPageable() {
        return pageable;
    }

    @Override
    public Q getEntityPath() {
        return entityPath;
    }

}
