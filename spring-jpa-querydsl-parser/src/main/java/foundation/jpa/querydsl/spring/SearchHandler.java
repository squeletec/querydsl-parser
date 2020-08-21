package foundation.jpa.querydsl.spring;

import foundation.jpa.querydsl.spring.impl.SearchEngineImpl;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.persistence.EntityManager;

import static foundation.jpa.querydsl.spring.SearchCriteriaHandlerBuilder.defaultCriteriaHandlerBuilder;

public class SearchHandler implements HandlerMethodArgumentResolver {

    private final SearchCriteriaHandler searchCriteriaHandler;
    private final SearchEngine searchEngine;

    public SearchHandler(SearchCriteriaHandler searchCriteriaHandler, SearchEngine searchEngine) {
        this.searchCriteriaHandler = searchCriteriaHandler;
        this.searchEngine = searchEngine;
    }

    public SearchHandler(EntityManager entityManager) {
        this(defaultCriteriaHandlerBuilder(), new SearchEngineImpl(entityManager));
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return Search.class.equals(methodParameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        SearchCriteria searchCriteria = searchCriteriaHandler.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
        return searchEngine.search(searchCriteria);
    }

}
