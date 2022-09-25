/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020-2020, Ondrej Fischer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package foundation.jpa.querydsl.spring.autoconfigure;

import foundation.jpa.querydsl.QueryVariables;
import foundation.jpa.querydsl.spring.*;
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
                                                       @Value("${querydsl.search.defaultPageSize:25}") int defaultPageSize) {
        return new SearchCriteriaHandler(defaultPageSize, defaultPage);
    }

    @Bean
    @ConditionalOnMissingBean
    public AggregationCriteriaHandler aggregateCriteriaHandler(@Value("${querydsl.search.defaultPage:0}") int defaultPage,
                                                               @Value("${querydsl.search.defaultPageSize:25}") int defaultPageSize) {
        return new AggregationCriteriaHandler(defaultPageSize, defaultPage);
    }

    @Bean
    @ConditionalOnMissingBean
    public SearchEngine searchEngine(EntityManager entityManager) {
        return new SearchEngineImpl(entityManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryVariables globalVariables(EntityManager entityManager) {
        return JpaQueryExecutor.enumValues(entityManager);
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
        resolvers.add(aggregateCriteriaHandler(0, 25));
    }

}
