package foundation.jpa.querydsl.spring;

import com.querydsl.core.types.EntityPath;

public interface SearchEngine {

    <E> SearchResult<E> search(SearchCriteria<? extends EntityPath<E>> criteria);

}
