package foundation.jpa.querydsl;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ManyToManyEntity {

    @Id
    @GeneratedValue
    private long id;

}
