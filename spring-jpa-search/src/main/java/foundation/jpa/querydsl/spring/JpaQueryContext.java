package foundation.jpa.querydsl.spring;

import foundation.jpa.querydsl.QueryContext;
import foundation.jpa.querydsl.QueryVariables;
import org.springframework.core.convert.ConversionService;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class JpaQueryContext extends QueryContext {

    public JpaQueryContext(ConversionService conversionService, EntityManager entityManager) {
        super(conversionService::convert);
    }

    public JpaQueryContext(EntityManager entityManager) {
        super((o, c) -> entityManager.find(c, o));
    }

    public static QueryVariables enumValues(EntityManager entityManager) {
        Map<String, Object> variables = new LinkedHashMap<>();
        entityManager.getMetamodel().getEntities().stream().flatMap(e -> e.getAttributes().stream()).map(Attribute::getJavaType).filter(Class::isEnum)
                .peek(c -> variables.put(c.getSimpleName(), c))
                .flatMap(e -> Stream.of(((Class<Enum<?>>)e).getEnumConstants())).forEach(e -> variables.put(e.name(), e));
        return QueryVariables.map(variables);
    }

}
