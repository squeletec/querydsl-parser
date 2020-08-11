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
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

@SpringBootTest
public class QueryFactoryTest extends AbstractTestNGSpringContextTests {

    @Inject
    private QueryContext queryContext;

    @Inject
    private RootEntityRepository repository;

    private boolean loaded = false;
    private Page<RootEntity> findAll(String query, int expectedSize) throws IOException {
        Page<RootEntity> page = repository.findAll(queryContext.parse(rootEntity, query), Pageable.unpaged());
        assertEquals(page.getSize(), expectedSize);
        return page;
    }

    @BeforeMethod
    public void load() {
        if(loaded) return;
        loaded = true;
        repository.save(new RootEntity().setName("ROOT1").setEnumValue(EnumValue.VALUE1).setSize(15).setManyToOneEntity(new ManyToOneEntity()).setManyToManyEntities(asList(
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

    @Test(expectedExceptions = SyntaxError.class, expectedExceptionsMessageRegExp = "Syntax error: No such field: oneToManyEee on entity rootEntity. Available fields are: rootEntity, enumValue, id, manyToManyEntities, manyToOneEntity, name, oneToManyEntities, size\n" +
            "\tat string: line: 1, character: 13")
    public void negativeTest() throws IOException {
        findAll("oneToManyEee.name = aaaROOT1", 1);
    }

    @Test
    public void varTest() throws IOException {
        findAll("enumValue = VALUE1", 1);
    }

    @Test
    public void constTest() throws IOException {
        findAll("enumValue = EnumValue.VALUE1", 1);
    }

    @Test(enabled = false)
    // TODO: Autowired ConversionService not taking JPA converter.
    public void entityTest() throws IOException {
        findAll("manyToOneEntity = 2", 0);
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
    public void notInTest() throws IOException {
        findAll("enumValue not in (VALUE1)", 1);
    }

    @Test
    public void testEmptyString() throws IOException {
        findAll("", 2);
    }

}
