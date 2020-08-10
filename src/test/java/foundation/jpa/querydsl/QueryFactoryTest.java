package foundation.jpa.querydsl;

import com.querydsl.jpa.impl.JPAQuery;
import foundation.rpg.parser.SyntaxError;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.List;

import static foundation.jpa.querydsl.QRootEntity.rootEntity;

@SpringBootTest
public class QueryFactoryTest extends AbstractTestNGSpringContextTests {

    @Inject
    private QueryContext queryContext;

    @Inject
    private EntityManager entityManager;

    private List<RootEntity> findAll(String query) throws IOException {
        return new JPAQuery<RootEntity>(entityManager).from(rootEntity).where(queryContext.parse(rootEntity, query)).fetch();
    }

    @Test
    public void test() throws IOException {
        queryContext.parse(rootEntity, "id = 1 and oneToManyEntity.id = 2");
    }

    @Test(expectedExceptions = SyntaxError.class, expectedExceptionsMessageRegExp = "Syntax error: No such field: oneToManyEee on entity rootEntity. Available fields are: rootEntity, enumValue, id, manyToManyEntities, manyToOneEntity, name, oneToManyEntities, size\n" +
            "\tat string: line: 1, character: 13")
    public void negativeTest() throws IOException {
        findAll("oneToManyEee.name = aaa");
    }

    @Test
    public void methodTest() throws IOException {
        findAll("toString() = 'aaa'");
    }

    @Test
    public void varTest() throws IOException {
        findAll("enumValue = VALUE1");
    }

    @Test
    public void constTest() throws IOException {
        findAll("enumValue = EnumValue.VALUE1");
    }

    @Test(enabled = false)
    // TODO: Autowired ConversionService not taking JPA converter.
    public void entityTest() throws IOException {
        findAll("manyToOneEntity = 2");
    }

    @Test
    public void manyToOneTest() throws IOException {
        findAll("oneToManyEntities.any.id = 3");
    }


    @Test
    public void notManyToOneTest() throws IOException {
        findAll("not(oneToManyEntities.any.id = 3)");
    }

}
