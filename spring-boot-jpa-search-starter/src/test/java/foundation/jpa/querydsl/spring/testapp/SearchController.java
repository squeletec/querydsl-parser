package foundation.jpa.querydsl.spring.testapp;

import foundation.jpa.querydsl.QueryVariables;
import foundation.jpa.querydsl.spring.*;
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
    public SearchResult<RootEntity> search(@CacheQuery @DefaultQuery("name='A'") Search<QRootEntity, RootEntity> result) {
        return result;
    }

    @GetMapping("/searchResult")
    public SearchResult<RootEntity> searchResult(SearchCriteria<QRootEntity> criteria) {
        return searchEngine.search(criteria, variables.get());
    }

}
