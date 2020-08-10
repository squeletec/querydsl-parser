package foundation.jpa.querydsl;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import javax.persistence.EntityManager;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;

@SpringBootConfiguration
//@AutoConfigureDataJpa
@EnableAutoConfiguration
@AutoConfigurationPackage
public class QueryFactoryConfig {

    public interface RootEntities extends QuerydslPredicateExecutor<RootEntity> {}

    @Bean
    public QueryContext context(ApplicationContext context) {
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put(EnumValue.class.getSimpleName(), EnumValue.class);
        Stream.of(EnumValue.class.getEnumConstants()).forEach(e -> vars.put(e.name(), e));
        DefaultListableBeanFactory autowireCapableBeanFactory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
        return QueryContext.createContext(autowireCapableBeanFactory.getConversionService(), vars);
    }

}
