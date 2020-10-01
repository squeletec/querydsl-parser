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

package foundation.jpa.querydsl.spring.testapp;

import com.querydsl.core.Tuple;
import foundation.jpa.querydsl.QueryVariables;
import foundation.jpa.querydsl.spring.*;
import foundation.jpa.querydsl.spring.impl.AggregateCriteriaImpl;
import foundation.jpa.querydsl.spring.impl.AggregationResultImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Provider;

@RestController
public class SearchController {

    private final SearchEngine searchEngine;
    private final Provider<QueryVariables> variables;

    public SearchController(SearchEngine searchEngine, Provider<QueryVariables> variables) {
        this.searchEngine = searchEngine;
        this.variables = variables;
    }

    @GetMapping("/search")
    public SearchResult<RootEntity> search(@CacheQuery @DefaultQuery("name='A'") Search<QRootEntity, RootEntity> query) {
        return query;
    }

    @GetMapping("/searchResult")
    public SearchResult<RootEntity> searchResult(SearchCriteria<QRootEntity> query) {
        return searchEngine.search(query, variables.get());
    }

    @GetMapping("/aggregation")
    public String aggregation(AggregateCriteria<QRootEntity> criteria) {
        return searchEngine.aggregate(new AggregateCriteriaImpl<>("q", "", "", PageRequest.of(0, 20), QRootEntity.rootEntity, "name", "name, count"), variables.get()).toString();
    }

}
