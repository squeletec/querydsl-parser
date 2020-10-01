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
