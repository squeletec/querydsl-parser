package foundation.jpa.querydsl.spring.autoconfigure;

import foundation.jpa.querydsl.QueryVariables;
import foundation.jpa.querydsl.spring.JpaQueryContext;
import foundation.jpa.querydsl.spring.SearchCriteriaHandler;
import foundation.jpa.querydsl.spring.SearchEngine;
import foundation.jpa.querydsl.spring.SearchHandler;
import foundation.jpa.querydsl.spring.impl.SearchEngineImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Provider;
import javax.persistence.EntityManager;

import java.util.List;

@Configuration
@ConditionalOnClass(EntityManager.class)
public class SearchConfiguration implements WebMvcConfigurer {

    @Bean
    @ConditionalOnMissingBean
    public SearchCriteriaHandler searchCriteriaHandler(@Value("${querydsl.search.defaultPage:0}") int defaultPage,
                                                       @Value("${querydsl.search.defaultPageSize:10}") int defaultPageSize) {
        return new SearchCriteriaHandler(defaultPageSize, defaultPage);
    }

    @Bean
    @ConditionalOnMissingBean
    public SearchEngine searchEngine(EntityManager entityManager) {
        return new SearchEngineImpl(entityManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryVariables globalVariables(EntityManager entityManager) {
        return JpaQueryContext.enumValues(entityManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public SearchHandler searchHandler(SearchCriteriaHandler searchCriteriaHandler, SearchEngine searchEngine, Provider<QueryVariables> variables) {
        return new SearchHandler(searchCriteriaHandler, searchEngine, variables);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(searchHandler(null, null, null));
        resolvers.add(searchCriteriaHandler(0, 0));
    }

}
