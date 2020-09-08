package foundation.jpa.querydsl.spring;

public class SearchCriteriaHandlerBuilder {

    private int defaultPageSize = 10;
    private int defaultPage = 0;

    public static SearchCriteriaHandlerBuilder searchCriteriaHandlerBuilder() {
        return new SearchCriteriaHandlerBuilder();
    }

    public static SearchCriteriaHandler defaultCriteriaHandlerBuilder() {
        return new SearchCriteriaHandlerBuilder().build();
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
        return new SearchCriteriaHandler(defaultPageSize, defaultPage);
    }

}
