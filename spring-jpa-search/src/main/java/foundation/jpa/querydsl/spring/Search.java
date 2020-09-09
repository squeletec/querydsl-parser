package foundation.jpa.querydsl.spring;

import com.querydsl.core.types.EntityPath;

public interface Search<Q extends EntityPath<E>, E> extends SearchResult<E> { }
