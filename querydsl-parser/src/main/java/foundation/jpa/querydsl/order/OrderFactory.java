package foundation.jpa.querydsl.order;

import com.querydsl.core.types.*;
import com.querydsl.core.types.Order;
import foundation.rpg.Name;
import foundation.rpg.StartSymbol;
import foundation.rpg.common.rules.NonEmpty;
import foundation.rpg.common.symbols.*;
import foundation.rpg.parser.Token;

import java.lang.Class;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

@SuppressWarnings("unused")
public class OrderFactory {

    private final EntityPath<?> root;

    public OrderFactory(EntityPath<?> root) {
        this.root = root;
    }

    @StartSymbol(parserClassName = "OrderByParser", lexerClassName = "OrderByLexer")
    Order[] is() {
        return new Order[]{};
    }

    Order[] is(@NonEmpty List<OrderSpecifier<?>> list) {
        return list.stream().map(OrderSpecifier::getOrder).toArray(Order[]::new);
    }

    OrderSpecifier<?>  is(Path<?> expression) {
        return new OrderSpecifier(Order.ASC, expression);
    }

    OrderSpecifier<?>  is1(Path<?> expression, @Name("asc") Token asc) {
        return new OrderSpecifier(Order.ASC, expression);
    }

    OrderSpecifier<?>  is2(Path<?> expression, @Name("desc") Token desc) {
        return new OrderSpecifier(Order.DESC, expression);
    }

    OrderSpecifier<?> is1 (OrderSpecifier<?> specifier, @Name("nulls") Token nulls, @Name("first") Token last) {
        return specifier.nullsFirst();
    }

    OrderSpecifier<?> is2 (OrderSpecifier<?> specifier, @Name("nulls") Token nulls, @Name("last") Token last) {
        return specifier.nullsLast();
    }

    Path<?> is (Identifier identifier) {
        String id = identifier.toString();
        return id.equals(root.toString()) ? root : is(root, null, identifier);
    }

    Path<?> is (Path<?> object, Dot operator, Identifier property) {
        String name = property.toString();
        Class<?> type = object.getClass();
        try {
            return (Path<?>) type.getField(name).get(object);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            try {
                return (Path<?>) type.getMethod(name).invoke(object);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                throw new IllegalArgumentException("No such field: " + name + " on entity " + object + ". Available fields are: " + Stream.of(object.getClass().getFields()).map(Field::getName).collect(joining(", ")), ex);
            }
        }
    }

    void ignore(WhiteSpace whiteSpace) {}


}
