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
    OrderSpecifier<?>[] is() {
        return new OrderSpecifier<?>[]{};
    }

    OrderSpecifier<?>[] is(@NonEmpty List<OrderSpecifier<?>> list) {
        return list.toArray(new OrderSpecifier<?>[]{});
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
