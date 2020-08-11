package foundation.jpa.querydsl;

import com.querydsl.core.annotations.QueryDelegate;

public final class QueryDelegates {

    private QueryDelegates() {}

    @QueryDelegate(RootEntity.class)
    public static QOneToManyEntity oneToManyEntity(QRootEntity rootEntity) {
        return rootEntity.oneToManyEntities.any();
    }

}
