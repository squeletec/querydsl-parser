package foundation.jpa.querydsl;

import java.util.Map;

public interface QueryVariables {

    Object get(String name);

    boolean isDefined(String name);

    static QueryVariables none() {
        return new QueryVariables() {
            @Override
            public Object get(String name) {
                return null;
            }

            @Override
            public boolean isDefined(String name) {
                return false;
            }
        };
    }

    static QueryVariables map(Map<String, Object> values) {
        return new QueryVariables() {
            @Override
            public Object get(String name) {
                return values.get(name);
            }

            @Override
            public boolean isDefined(String name) {
                return values.containsKey(name);
            }
        };
    }

    static QueryVariables local(Map<String, Object> values, QueryVariables parent) {
        return new QueryVariables() {
            @Override
            public Object get(String name) {
                return values.containsKey(name) ? values.get(name) : parent.get(name);
            }

            @Override
            public boolean isDefined(String name) {
                return values.containsKey(name) || parent.isDefined(name);
            }
        };
    }

}
