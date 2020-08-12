package foundation.jpa.querydsl.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootApplication
public class SearchApplication implements WebMvcConfigurer {

    public static void main(String... args) {
        SpringApplication.run(SearchApplication.class);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(searchParameterHandler(null));
    }

    @Bean
    public SearchParameterHandler searchParameterHandler(EntityManager manager) {
        return new SearchParameterHandler(manager);
    }

}
