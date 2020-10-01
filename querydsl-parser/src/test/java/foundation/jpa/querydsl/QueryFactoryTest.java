/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2020-2020, Ondrej Fischer
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

package foundation.jpa.querydsl;

import foundation.rpg.parser.SyntaxError;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.IOException;

import static foundation.jpa.querydsl.QRootEntity.rootEntity;
import static foundation.jpa.querydsl.QueryVariables.local;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;

@SpringBootTest
public class QueryFactoryTest extends AbstractTestNGSpringContextTests {

    @Inject
    private QueryContext queryContext;

    @Inject
    private RootEntityRepository repository;

    @Inject
    private ManyToOneEntityRepository manyToOneEntityRepository;

    @Inject
    private QueryVariables variables;

    private boolean loaded = false;
    private Page<RootEntity> findAll(String query, int expectedSize) throws IOException {
        Page<RootEntity> page = repository.findAll(queryContext.parsePredicate(rootEntity, query, variables), Pageable.unpaged());
        assertEquals(page.getSize(), expectedSize);
        return page;
    }

    @BeforeMethod
    public void load() {
        if(loaded) return;
        loaded = true;
        repository.save(new RootEntity().setName("ROOT1").setEnumValue(EnumValue.VALUE1).setSize(15).setIntValue(1).setManyToOneEntity(new ManyToOneEntity()).setManyToManyEntities(asList(
                new ManyToManyEntity(), new ManyToManyEntity()
        )).setOneToManyEntities(asList(
                new OneToManyEntity().setString("A"), new OneToManyEntity().setString("B"), new OneToManyEntity().setString("C")
        )));
        repository.save(new RootEntity().setName("ROOT2").setEnumValue(EnumValue.VALUE2).setSize(0).setManyToOneEntity(new ManyToOneEntity()).setManyToManyEntities(asList(
                new ManyToManyEntity(), new ManyToManyEntity()
        )).setOneToManyEntities(asList(
                new OneToManyEntity().setString("D"), new OneToManyEntity().setString("A")
        )));
    }

    @Test
    public void test() throws IOException {
        Page<RootEntity> all = findAll("name = 'ROOT1' and oneToManyEntity.string = 'B'", 1);
    }

    @Test(expectedExceptions = SyntaxError.class, expectedExceptionsMessageRegExp = "Syntax error: No such field: oneToManyEee on entity rootEntity. Available fields are: rootEntity, enumValue, id, intValue, manyToManyEntities, manyToOneEntity, name, oneToManyEntities, size\n" +
            "\tat string: line: 1, character: 13")
    public void negativeTest() throws IOException {
        findAll("oneToManyEee.name = aaaROOT1", 1);
    }

    @Test
    public void varTest() throws IOException {
        findAll("enumValue = VALUE1", 1);
    }

    @Test
    public void gtTest() throws IOException {
        findAll("size > 1", 1);
    }

    @Test
    public void plusTest() throws IOException {
        findAll("size + intValue = 16", 1);
    }

    @Test
    public void constTest() throws IOException {
        findAll("enumValue = EnumValue.VALUE1", 1);
    }

    @Test
    public void entityTest() throws IOException {
        findAll("manyToOneEntity = 2", 1);
    }

    @Test
    public void entityNoConvertTest() throws IOException {
        ManyToOneEntity e2 = manyToOneEntityRepository.getOne(2L);
        Page<RootEntity> page = repository.findAll(queryContext.parsePredicate(rootEntity, "manyToOneEntity = e2", local(singletonMap("e2", e2), variables)), Pageable.unpaged());
        assertEquals(page.getSize(), 1);
    }

    @Test(expectedExceptions = SyntaxError.class, expectedExceptionsMessageRegExp = "Syntax error: No ManyToOneEntity 13 exists!\n" +
            "\tat string: line: 1, character: 21")
    public void negativeEntityTest() throws IOException {
        findAll("manyToOneEntity = 13", 0);
    }

    @Test
    public void manyToOneTest() throws IOException {
        findAll("oneToManyEntities.any.string = 'A'", 2);
    }

    @Test
    public void notManyToOneTest() throws IOException {
        findAll("not(oneToManyEntities.any.string = 'B')", 1);
    }

    @Test
    public void inTest() throws IOException {
        findAll("enumValue in (EnumValue.VALUE1, VALUE2)", 2);
    }

    @Test
    public void inLiteralTest() throws IOException {
        findAll("name in ('ROOT1', 'ROOT2')", 2);
    }

    @Test
    public void notInTest() throws IOException {
        findAll("enumValue not in (VALUE1)", 1);
    }

    @Test
    public void testEmptyString() throws IOException {
        findAll("", 2);
    }

}
