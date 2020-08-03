package foundation.jpa.query;

@FunctionalInterface
public interface EntityConstantResolver {

    Object resolveEntity(Class<?> entityType, Object id);

}
