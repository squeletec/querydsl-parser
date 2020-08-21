package foundation.jpa.querydsl.spring;

import foundation.jpa.querydsl.QueryContext;

import javax.persistence.EntityManager;

import static java.util.Objects.isNull;

public class SearchParameterHandlerBuilder {

    private final EntityManager entityManager;
    private QueryContext queryContext;
    private String queryParameterName = "query";
    private String sortParameterName = "sort";
    private String pageParameterName = "page";
    private String sizeParameterName = "size";
    private int defaultPageSize = 10;
    private int defaultPage = 0;

    private SearchParameterHandlerBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public static SearchParameterHandlerBuilder searchParameterHandlerBuilder(EntityManager entityManager) {
        return new SearchParameterHandlerBuilder(entityManager);
    }

    public SearchParameterHandlerBuilder setQueryContext(QueryContext queryContext) {
        this.queryContext = queryContext;
        return this;
    }

    public SearchParameterHandlerBuilder setQueryParameterName(String queryParameterName) {
        this.queryParameterName = queryParameterName;
        return this;
    }

    public SearchParameterHandlerBuilder setSortParameterName(String sortParameterName) {
        this.sortParameterName = sortParameterName;
        return this;
    }

    public SearchParameterHandlerBuilder setPageParameterName(String pageParameterName) {
        this.pageParameterName = pageParameterName;
        return this;
    }

    public SearchParameterHandlerBuilder setSizeParameterName(String sizeParameterName) {
        this.sizeParameterName = sizeParameterName;
        return this;
    }

    public SearchParameterHandlerBuilder setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
        return this;
    }

    public SearchParameterHandlerBuilder setDefaultPage(int defaultPage) {
        this.defaultPage = defaultPage;
        return this;
    }

    public SearchParameterHandler build() {
        return new SearchParameterHandler(
                entityManager,
                isNull(queryContext) ? new JpaQueryContext(entityManager) : queryContext,
                queryParameterName,
                sortParameterName,
                pageParameterName,
                sizeParameterName,
                defaultPageSize,
                defaultPage
        );
    }
}
