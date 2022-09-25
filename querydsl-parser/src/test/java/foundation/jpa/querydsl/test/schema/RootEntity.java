/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020-2022, Ondrej Fischer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package foundation.jpa.querydsl.test.schema;

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

    private int intValue;

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

    public int getIntValue() {
        return intValue;
    }

    public RootEntity setIntValue(int intValue) {
        this.intValue = intValue;
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
