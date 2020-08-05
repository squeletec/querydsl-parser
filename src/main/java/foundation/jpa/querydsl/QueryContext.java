package foundation.jpa.querydsl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import foundation.rpg.parser.SyntaxError;
import org.springframework.core.convert.ConversionService;

import java.io.IOException;
import java.util.Map;

public final class QueryContext {

    private final ConversionService conversionService;
    private final Map<String, Object> variables;

    public QueryContext(ConversionService conversionService, Map<String, Object> variables) {
        this.conversionService = conversionService;
        this.variables = variables;
    }

    public static QueryContext createContext(ConversionService conversionService, Map<String, Object> variables) {
        return new QueryContext(conversionService, variables);
    }

    public Predicate parse(EntityPath<?> entityPath, String query) throws IOException, SyntaxError {
        return new PredicateParser(new QueryFactory(conversionService, variables, entityPath)).parseString(query);
    }

}
