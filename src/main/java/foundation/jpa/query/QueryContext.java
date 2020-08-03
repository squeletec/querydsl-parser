package foundation.jpa.query;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import foundation.rpg.parser.SyntaxError;

import java.io.IOException;
import java.util.Map;

public final class QueryContext {

    private final EntityConstantResolver entityConstantResolver;
    private final Map<String, Object> variables;

    public QueryContext(EntityConstantResolver entityConstantResolver, Map<String, Object> variables) {
        this.entityConstantResolver = entityConstantResolver;
        this.variables = variables;
    }

    public static QueryContext createContext(EntityConstantResolver entityConstantResolver, Map<String, Object> variables) {
        return new QueryContext(entityConstantResolver, variables);
    }

    public Predicate parse(EntityPath<?> entityPath, String query) throws IOException, SyntaxError {
        return new PredicateParser(new QueryFactory(entityConstantResolver, variables, entityPath)).parseString(query);
    }

}
