package foundation.jpa.querydsl.spring.testapp;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RootEntityRepository extends JpaRepository<RootEntity, Long> {
}
