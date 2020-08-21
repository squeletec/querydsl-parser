package foundation.jpa.querydsl.spring;

public class SearchCriteriaHandlerBuilder {

    private String queryParameterName = "query";
    private String sortParameterName = "sort";
    private String pageParameterName = "page";
    private String sizeParameterName = "size";
    private int defaultPageSize = 10;
    private int defaultPage = 0;

    public static SearchCriteriaHandlerBuilder searchCriteriaHandlerBuilder() {
        return new SearchCriteriaHandlerBuilder();
    }

    public static SearchCriteriaHandler defaultCriteriaHandlerBuilder() {
        return new SearchCriteriaHandlerBuilder().build();
    }

    public SearchCriteriaHandlerBuilder setQueryParameterName(String queryParameterName) {
        this.queryParameterName = queryParameterName;
        return this;
    }

    public SearchCriteriaHandlerBuilder setSortParameterName(String sortParameterName) {
        this.sortParameterName = sortParameterName;
        return this;
    }

    public SearchCriteriaHandlerBuilder setPageParameterName(String pageParameterName) {
        this.pageParameterName = pageParameterName;
        return this;
    }

    public SearchCriteriaHandlerBuilder setSizeParameterName(String sizeParameterName) {
        this.sizeParameterName = sizeParameterName;
        return this;
    }

    public SearchCriteriaHandlerBuilder setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
        return this;
    }

    public SearchCriteriaHandlerBuilder setDefaultPage(int defaultPage) {
        this.defaultPage = defaultPage;
        return this;
    }

    public SearchCriteriaHandler build() {
        return new SearchCriteriaHandler(queryParameterName, sortParameterName, pageParameterName, sizeParameterName, defaultPageSize, defaultPage);
    }

}
