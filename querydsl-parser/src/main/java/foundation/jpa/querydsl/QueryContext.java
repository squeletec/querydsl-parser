package foundation.jpa.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import foundation.jpa.querydsl.order.OrderByParser;
import foundation.jpa.querydsl.order.OrderFactory;
import foundation.rpg.parser.SyntaxError;

import java.io.IOException;

public class QueryContext {

    private final EntityConverter entityConverter;

    public QueryContext(EntityConverter entityConverter) {
        this.entityConverter = entityConverter;
    }

    public static QueryContext createContext(EntityConverter conversionService) {
        return new QueryContext(conversionService);
    }

    public static QueryContext createContext() {
        return new QueryContext(EntityConverter.noConversion());
    }

    public Predicate parsePredicate(EntityPath<?> entityPath, String query, QueryVariables queryVariables) throws IOException, SyntaxError {
        if(query == null || query.isEmpty())
            return new BooleanBuilder().and(null);
        return new PredicateParser(new QueryFactory(entityConverter, queryVariables, entityPath)).parseString(query);
    }

    public OrderSpecifier<?>[] parseOrderSpecifier(EntityPath<?> entityPath, String orderBy) throws IOException {
        if(orderBy == null)
            return new OrderSpecifier[0];
        return new OrderByParser(new OrderFactory(entityPath)).parseString(orderBy);
    }
}
