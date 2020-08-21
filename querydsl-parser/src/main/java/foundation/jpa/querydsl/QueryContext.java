package foundation.jpa.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import foundation.jpa.querydsl.order.OrderByParser;
import foundation.jpa.querydsl.order.OrderFactory;
import foundation.rpg.parser.SyntaxError;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class QueryContext {

    private final EntityConverter entityConverter;
    private final Map<String, Object> variables;

    public QueryContext(EntityConverter entityConverter, Map<String, Object> variables) {
        this.entityConverter = entityConverter;
        this.variables = variables;
    }

    public static QueryContext createContext(EntityConverter conversionService, Map<String, Object> variables) {
        return new QueryContext(conversionService, variables);
    }

    public static QueryContext createContext(Map<String, Object> variables) {
        return new QueryContext(EntityConverter.noConversion(), variables);
    }

    public static QueryContext createContext() {
        return new QueryContext(EntityConverter.noConversion(), emptyMap());
    }

    public Predicate parse(EntityPath<?> entityPath, String query) throws IOException, SyntaxError {
        if(query == null || query.isEmpty())
            return new BooleanBuilder().and(null);
        return new PredicateParser(new QueryFactory(entityConverter, variables, entityPath)).parseString(query);
    }

    public OrderSpecifier<?>[] parseOrderSpecifier(EntityPath<?> entityPath, String orderBy) throws IOException {
        if(orderBy == null)
            return new OrderSpecifier[0];
        return new OrderByParser(new OrderFactory(entityPath)).parseString(orderBy);
    }
}
