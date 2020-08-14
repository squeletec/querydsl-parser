package foundation.jpa.querydsl.spring;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;

import java.net.URI;

public interface Search<E, Q extends EntityPath<E>> {

    String getQuery();

    String getSort();

    Page<E> getPage();

    Throwable getError();

    URI getUri();

    static <E, Q extends EntityPath<E>> Search<E, Q> search(String query, String sort, Predicate predicate, OrderSpecifier<?>[] orderSpecifiers, Page<E> page, Throwable error, URI uri) {
        return new Search<E, Q>() {
            @Override
            public String getQuery() {
                return query;
            }

            @Override
            public String getSort() {
                return sort;
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
            public URI getUri() {
                return uri;
            }
        };
    }
}
