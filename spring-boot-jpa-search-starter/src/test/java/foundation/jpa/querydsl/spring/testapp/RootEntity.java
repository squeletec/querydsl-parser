package foundation.jpa.querydsl.spring.testapp;

import javax.persistence.*;
import java.util.List;

@Entity
public class RootEntity {

    @Id
    @GeneratedValue
    private long id;

    @OneToMany(cascade = CascadeType.ALL)
    private List<OneToManyEntity> oneToManyEntities;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<ManyToManyEntity> manyToManyEntities;

    @ManyToOne(cascade = CascadeType.ALL)
    private ManyToOneEntity manyToOneEntity;

    private String name;

    private int size;

    private EnumValue enumValue;

    public long getId() {
        return id;
    }

    public RootEntity setId(long id) {
        this.id = id;
        return this;
    }

    public List<OneToManyEntity> getOneToManyEntities() {
        return oneToManyEntities;
    }

    public RootEntity setOneToManyEntities(List<OneToManyEntity> oneToManyEntities) {
        this.oneToManyEntities = oneToManyEntities;
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

    public EnumValue getEnumValue() {
        return enumValue;
    }

    public RootEntity setEnumValue(EnumValue enumValue) {
        this.enumValue = enumValue;
        return this;
    }

}
