package foundation.jpa.querydsl.spring.impl;

import com.querydsl.core.types.EntityPath;
import foundation.jpa.querydsl.spring.AggregateCriteria;
import org.springframework.data.domain.Pageable;

public class AggregateCriteriaImpl<E extends EntityPath<?>> extends SearchCriteriaImpl<E> implements AggregateCriteria<E> {

    private final String groupBy;
    private final String select;

    public AggregateCriteriaImpl(String parameterName, String query, String sort, Pageable pageable, E entityPath, String groupBy, String select) {
        super(parameterName, query, sort, pageable, entityPath);
        this.groupBy = groupBy;
        this.select = select;
    }

    @Override
    public String groupBy() {
        return groupBy;
    }

    @Override
    public String select() {
        return select;
    }

}
