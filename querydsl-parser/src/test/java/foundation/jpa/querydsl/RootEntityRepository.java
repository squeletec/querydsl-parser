package foundation.jpa.querydsl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface RootEntityRepository extends JpaRepository<RootEntity, Long>, QuerydslPredicateExecutor<RootEntity> {
}
