package foundation.jpa.querydsl;

public interface EntityConverter {

    Object convert(Object constant, Class<?> type);

    static EntityConverter noConversion() {
        return (constant, type) -> constant;
    }

}
