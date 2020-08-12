package foundation.jpa.querydsl.spring;

import com.querydsl.core.types.EntityPath;
import org.springframework.data.domain.Page;

import java.net.URI;

public interface Search<E, Q extends EntityPath<E>> {

    String getQuery();

    Page<E> getPage();

    Throwable getError();

    URI getUri();

    static <E, Q extends EntityPath<E>> Search<E, Q> search(String query, Page<E> page, Throwable error, URI uri) {
        return new Search<E, Q>() {
            @Override
            public String getQuery() {
                return query;
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
