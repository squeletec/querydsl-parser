package foundation.jpa.querydsl.spring.testapp;

import foundation.jpa.querydsl.QueryVariables;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Named;
import javax.persistence.EntityManager;

import java.util.Collections;

import static foundation.jpa.querydsl.spring.JpaQueryContext.enumValues;
import static java.util.Arrays.asList;

@SpringBootApplication
public class SearchApplication implements WebMvcConfigurer {

    public static void main(String... args) {
        SpringApplication.run(SearchApplication.class);
    }

    @Bean
    @Named("global")
    public QueryVariables globalVariables(EntityManager entityManager) {
        return enumValues(entityManager);
    }

    @Primary
    @Bean
    @Scope(WebApplicationContext.SCOPE_SESSION)
    public QueryVariables sessionVariables(@Named("global") QueryVariables globalVariables) {
        return QueryVariables.local(Collections.singletonMap("local", "value"), globalVariables);
    }

    @Bean
    public boolean data(RootEntityRepository repository) {
        repository.save(new RootEntity().setName("ROOT1").setEnumValue(EnumValue.VALUE1).setSize(15).setManyToOneEntity(new ManyToOneEntity()).setManyToManyEntities(asList(
                new ManyToManyEntity(), new ManyToManyEntity()
        )).setOneToManyEntities(asList(
                new OneToManyEntity().setString("A"), new OneToManyEntity().setString("B"), new OneToManyEntity().setString("C")
        )));
        repository.save(new RootEntity().setName("ROOT2").setEnumValue(EnumValue.VALUE2).setSize(0).setManyToOneEntity(new ManyToOneEntity()).setManyToManyEntities(asList(
                new ManyToManyEntity(), new ManyToManyEntity()
        )).setOneToManyEntities(asList(
                new OneToManyEntity().setString("D"), new OneToManyEntity().setString("A")
        )));
        return true;
    }

}
