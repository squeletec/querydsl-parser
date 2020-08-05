package foundation.jpa.querydsl;

import javax.persistence.*;
import java.util.List;

@Entity
public class RootEntity {

    @Id
    private long id;

    @OneToMany
    private List<OneToManyEntity> oneToManyEntity;

    @ManyToMany
    private List<ManyToManyEntity> manyToManyEntities;

    @ManyToOne
    private ManyToOneEntity manyToOneEntity;

    private String name;

    private int size;

    public long getId() {
        return id;
    }

    public RootEntity setId(long id) {
        this.id = id;
        return this;
    }

    public List<OneToManyEntity> getOneToManyEntity() {
        return oneToManyEntity;
    }

    public RootEntity setOneToManyEntity(List<OneToManyEntity> oneToManyEntity) {
        this.oneToManyEntity = oneToManyEntity;
        return this;
    }

    public List<ManyToManyEntity> getManyToManyEntities() {
        return manyToManyEntities;
    }

    public RootEntity setManyToManyEntities(List<ManyToManyEntity> manyToManyEntities) {
        this.manyToManyEntities = manyToManyEntities;
        return this;
    }

    public ManyToOneEntity getManyToOneEntity() {
        return manyToOneEntity;
    }

    public RootEntity setManyToOneEntity(ManyToOneEntity manyToOneEntity) {
        this.manyToOneEntity = manyToOneEntity;
        return this;
    }

    public String getName() {
        return name;
    }

    public RootEntity setName(String name) {
        this.name = name;
        return this;
    }

    public int getSize() {
        return size;
    }

    public RootEntity setSize(int size) {
        this.size = size;
        return this;
    }
}
