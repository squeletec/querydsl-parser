package foundation.jpa.querydsl.spring;

import com.querydsl.core.types.EntityPath;
import org.springframework.data.domain.Page;

import java.net.URI;

public interface Search<E, Q extends EntityPath<E>> {

    String query();

    Page<E> page();

    Throwable error();

    URI uri();

    static <E, Q extends EntityPath<E>> Search<E, Q> search(String query, Page<E> page, Throwable error, URI uri) {
        return new Search<E, Q>() {
            @Override
            public String query() {
                return query;
            }

            @Override
            public Page<E> page() {
                return page;
            }

            @Override
            public Throwable error() {
                return error;
            }

            @Override
            public URI uri() {
                return uri;
            }
        };
    }
}
