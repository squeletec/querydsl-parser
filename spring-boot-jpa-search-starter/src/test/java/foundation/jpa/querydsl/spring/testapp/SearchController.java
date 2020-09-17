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
