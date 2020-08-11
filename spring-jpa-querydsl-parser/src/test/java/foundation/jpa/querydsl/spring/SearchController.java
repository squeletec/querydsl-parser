package foundation.jpa.querydsl.spring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    @GetMapping("/search")
    public Search<RootEntity, QRootEntity> search(Search<RootEntity, QRootEntity> result) {
        return result;
    }

}
