package foundation.jpa.querydsl.spring;

import foundation.jpa.querydsl.QueryVariables;
import foundation.jpa.querydsl.spring.impl.SearchEngineImpl;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.inject.Provider;
import javax.persistence.EntityManager;

import static foundation.jpa.querydsl.spring.SearchCriteriaHandlerBuilder.defaultCriteriaHandlerBuilder;

public class SearchHandler implements HandlerMethodArgumentResolver {

    private final SearchCriteriaHandler searchCriteriaHandler;
    private final SearchEngine searchEngine;
    private final Provider<QueryVariables> variables;

    public SearchHandler(SearchCriteriaHandler searchCriteriaHandler, SearchEngine searchEngine, Provider<QueryVariables> variables) {
        this.searchCriteriaHandler = searchCriteriaHandler;
        this.searchEngine = searchEngine;
        this.variables = variables;
    }

    public SearchHandler(EntityManager entityManager, Provider<QueryVariables> variables) {
        this(defaultCriteriaHandlerBuilder(), new SearchEngineImpl(entityManager), variables);
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return Search.class.equals(methodParameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        SearchCriteria searchCriteria = searchCriteriaHandler.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
        return searchEngine.search(searchCriteria, variables.get());
    }

}
