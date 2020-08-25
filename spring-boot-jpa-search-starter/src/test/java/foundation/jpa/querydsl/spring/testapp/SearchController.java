package foundation.jpa.querydsl.spring.testapp;

import foundation.jpa.querydsl.spring.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    private final SearchEngine searchEngine;

    public SearchController(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    @GetMapping("/search")
    public SearchResult<RootEntity> search(@CacheQuery @DefaultQuery("name='A'") Search<QRootEntity, RootEntity> result) {
        return result;
    }

    @GetMapping("/searchResult")
    public SearchResult<RootEntity> searchResult(SearchCriteria<QRootEntity> criteria) {
        return searchEngine.search(criteria);
    }

}
