package foundation.jpa.querydsl;

import foundation.rpg.parser.SyntaxError;
import org.testng.annotations.Test;

import java.io.IOException;

import static foundation.jpa.querydsl.QRootEntity.rootEntity;
import static java.util.Collections.emptyMap;

public class QueryFactoryTest {

    private final QueryContext queryContext = QueryContext.createContext(null, emptyMap());

    @Test
    public void test() throws IOException {
        queryContext.parse(rootEntity, "id = 1 and oneToManyEntity.id = 2");
    }

    @Test(expectedExceptions = SyntaxError.class, expectedExceptionsMessageRegExp = "Syntax error: No such field: oneToManyEee on entity rootEntity. Available fields are: rootEntity, id, manyToManyEntities, manyToOneEntity, name, oneToManyEntity, size\n" +
            "\tat string: line: 1, character: 13")
    public void negativeTest() throws IOException {
        queryContext.parse(rootEntity, "oneToManyEee.name = aaa");
    }

    @Test
    public void methodTest() throws IOException {
        queryContext.parse(rootEntity, "toString() = 'aaa'");
    }
}
