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

package foundation.jpa.querydsl.test;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import foundation.jpa.querydsl.QueryExecutor;
import foundation.jpa.querydsl.QueryVariables;
import foundation.jpa.querydsl.test.schema.*;
import foundation.rpg.parser.SyntaxError;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import static foundation.jpa.querydsl.QueryVariables.local;
import static foundation.jpa.querydsl.test.schema.QDocument.document;
import static foundation.jpa.querydsl.test.schema.QRootEntity.rootEntity;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@SpringBootTest(classes = QueryFactoryConfig.class)
public class QueryFactoryTest extends AbstractTestNGSpringContextTests {

    @Inject
    private JPAQueryFactory queryFactory;

    @Inject
    private QueryExecutor queryExecutor;

    @Inject
    private RootEntityRepository repository;

    @Inject
    private DocumentTypeRepository documentTypeRepository;

    @Inject
    private DocumentRepository documentRepository;

    @Inject
    private FieldTypeRepository fieldTypeRepository;

    @Inject
    private ManyToOneEntityRepository manyToOneEntityRepository;

    @Inject
    private QueryVariables variables;

    private boolean loaded = false;
    private Page<RootEntity> findAll(String query, int expectedSize) throws IOException {
        Page<RootEntity> page = repository.findAll(queryExecutor.parsePredicate(rootEntity, query, variables), Pageable.unpaged());
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
                new OneToManyEntity().setString("D\r"), new OneToManyEntity().setString("A")
        )));
        FieldType string = fieldTypeRepository.save(new FieldType().setName("string"));
        DocumentType story = documentTypeRepository.save(new DocumentType().setName("Story").setDescription("Aha").setFields(asList(
                new Field().setName("state").setType(string),
                new Field().setName("priority").setType(string)
        )));
        Document newStory = new Document().setDocumentType(story).setName("New story").setValues(new LinkedHashMap<>());
        for(Field f : story.getFields()) {
            newStory.getValues().put(f.getName(), new FieldValue().setField(f).setName(f.getName()));
        }
        newStory.getValues().get("state").setData("New");
        newStory.getValues().get("priority").setData("Critical");
        documentRepository.save(newStory);
        Document openStory = new Document().setDocumentType(story).setName("Open story").setValues(new LinkedHashMap<>());
        for(Field f : story.getFields()) {
            openStory.getValues().put(f.getName(), new FieldValue().setField(f).setName(f.getName()));
        }
        openStory.getValues().get("state").setData("Open");
        openStory.getValues().get("priority").setData("Major");
        documentRepository.save(openStory);
    }

    @Test
    public void test() throws IOException {
        Page<RootEntity> all = findAll("name = 'ROOT1' and oneToManyEntity.string = 'B'", 1);
        assertEquals(all.getSize(), 1);
    }

    @Test
    public void testLike() throws IOException {
        Page<RootEntity> all = findAll("name ~ 'ROOT1'", 1);
        assertEquals(all.getSize(), 1);
    }

    @Test
    public void testLike2() throws IOException {
        Page<RootEntity> all = findAll("name ~ 'ROO%'", 2);
        assertEquals(all.getSize(), 2);
    }

    @Test
    public void testMap() {
        Page<Document> page = documentRepository.findAll(document.get("state").eq("New"), Pageable.unpaged());
        assertEquals(page.getSize(), 1);
    }

    @Test
    public void testMapQuery() throws IOException {
        Page<Document> page = documentRepository.findAll(queryExecutor.parsePredicate(document, "state = 'New' and priority = 'Critical'", variables), Pageable.unpaged());
        assertEquals(page.getSize(), 1);
    }

    @Test
    public void testFullMapQuery() throws IOException {
        Page<Document> page = documentRepository.findAll(queryExecutor.parsePredicate(document, "values.state.data = 'New'", variables), Pageable.unpaged());
        assertEquals(page.getSize(), 1);
    }

    @Test(expectedExceptions = SyntaxError.class, expectedExceptionsMessageRegExp = "Syntax error: No such property: oneToManyEee on rootEntity. Available properties are: rootEntity, enumValue, id, intValue, manyToManyEntities, manyToOneEntity, name, oneToManyEntities, size\n" +
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
    public void entityInTest() throws IOException {
        findAll("manyToOneEntity in (2, 2)", 1);
    }

    @Test
    public void entityNoConvertTest() throws IOException {
        ManyToOneEntity e2 = manyToOneEntityRepository.getReferenceById(2L);
        Page<RootEntity> page = repository.findAll(queryExecutor.parsePredicate(rootEntity, "manyToOneEntity = e2", local(singletonMap("e2", e2), variables)), Pageable.unpaged());
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
    public void oneToManyRTest() throws IOException {
        findAll("oneToManyEntities.any.string = 'D\\r'", 1);
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

    private List<List<?>> select(String fields) throws IOException {
        return queryFactory.selectFrom(rootEntity).select(Projections.list(queryExecutor.parseSelect(rootEntity, fields, variables))).fetch();
    }

    @Test
    public void testSelect() throws IOException {
        System.out.println(select("sum(size > 1)"));
    }

    @Test
    public void testSelect2() throws IOException {
        System.out.println(select("sum(case size > 1 when true then 1 else 0)"));
    }

    @Test
    public void testSelectCount() throws IOException {
        System.out.println(select("count(rootEntity)"));
    }

    @Test
    public void testSelectSum() throws IOException {
        System.out.println(select("sum(size)"));
    }

    @Test
    public void testSelectSpread() throws IOException {
        System.out.println(select("min(size) as minSize, max(size), max(size) - min(size)"));
        assertNotNull(variables.get("minSize"));
    }

    @Test
    public void testCallContains() throws IOException {
        System.out.println(select("name.contains('A')"));
    }

    @Test
    public void testCallVararg() throws IOException {
        System.out.println(select("name.coalesce('A', 'B')"));
    }

    @Test(expectedExceptions = SyntaxError.class, expectedExceptionsMessageRegExp = "Syntax error: No such method: nonsense on rootEntity\\.name with 1 parameters\\..*")
    public void testCallNonexistentMethod() throws IOException {
        System.out.println(select("name.nonsense('A')"));
    }

}
