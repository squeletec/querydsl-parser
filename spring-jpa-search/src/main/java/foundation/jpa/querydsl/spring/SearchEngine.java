package foundation.jpa.querydsl.spring;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;

public interface SearchEngine {

    <E> SearchResult<E> search(SearchCriteria<? extends EntityPath<E>> criteria);

    <E> SearchResult<E> search(Predicate implicitPredicate, SearchCriteria<? extends EntityPath<E>> criteria);

    <E> SearchResult<E> search(EntityPath<E> entityPath, String query, String sort, Pageable pageable);

}
