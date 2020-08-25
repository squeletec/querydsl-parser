package foundation.jpa.querydsl.spring;

import com.querydsl.core.types.EntityPath;
import org.springframework.data.domain.Page;

public interface SearchResult<E> {

    SearchCriteria<? extends EntityPath<E>> getCriteria();

    Page<E> getPage();

    Throwable getError();

    boolean hasError();

}
