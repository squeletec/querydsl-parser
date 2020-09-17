package foundation.jpa.querydsl.groupby;

import com.querydsl.core.types.*;
import foundation.rpg.StartSymbol;
import foundation.rpg.common.rules.NonEmpty;
import foundation.rpg.common.symbols.Dot;
import foundation.rpg.common.symbols.Identifier;
import foundation.rpg.common.symbols.WhiteSpace;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@SuppressWarnings("unused")
public class GroupByFactory {

    private final EntityPath<?> root;

    public GroupByFactory(EntityPath<?> root) {
        this.root = root;
    }

    @StartSymbol(parserClassName = "GroupByParser", lexerClassName = "GroupByLexer")
    Expression<?>[] is() {
        return new Expression<?>[0];
    }

    Expression<?>[] is(@NonEmpty List<Expression<?>> list) {
        return list.toArray(new Expression<?>[]{});
    }

    Expression<?> is (Identifier identifier) {
        String id = identifier.toString();
        return id.equals(root.toString()) ? root : is(root, null, identifier);
    }

    Expression<?> is (Expression<?> object, Dot operator, Identifier property) {
        String name = property.toString();
        Class<?> type = object.getClass();
        try {
            return (Expression<?>) type.getField(name).get(object);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            try {
                return (Expression<?>) type.getMethod(name).invoke(object);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                throw new IllegalArgumentException("No such field: " + name + " on entity " + object + ". Available fields are: " + Stream.of(object.getClass().getFields()).map(Field::getName).collect(joining(", ")), ex);
            }
        }
    }

    void ignore(WhiteSpace whiteSpace) {}

}
