package foundation.jpa.querydsl.spring;

import com.querydsl.core.types.EntityPath;

public interface AggregateCriteria<E extends EntityPath<?>> extends SearchCriteria<E> {

    String groupBy();

    String select();

}
