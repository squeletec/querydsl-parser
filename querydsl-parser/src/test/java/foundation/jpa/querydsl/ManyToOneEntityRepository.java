package foundation.jpa.querydsl;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ManyToOneEntityRepository extends JpaRepository<ManyToOneEntity, Long> { }
