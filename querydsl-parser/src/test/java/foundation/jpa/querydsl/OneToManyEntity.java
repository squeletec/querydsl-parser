package foundation.jpa.querydsl;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class OneToManyEntity {

    @Id
    @GeneratedValue
    private long id;

    private String string;

    public long getId() {
        return id;
    }

    public OneToManyEntity setId(long id) {
        this.id = id;
        return this;
    }

    public String getString() {
        return string;
    }

    public OneToManyEntity setString(String string) {
        this.string = string;
        return this;
    }
}
