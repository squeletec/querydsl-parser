package foundation.jpa.querydsl.spring;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.types.EntityPath;
import org.springframework.data.domain.Pageable;

public interface SearchCriteria<E extends EntityPath<?>> {

    boolean hasImplicitQuery();

    String getImplicitQuery();

    String getQuery();

    String getSort();

    Pageable getPageable();

    @JsonIgnore
    E getEntityPath();

}
