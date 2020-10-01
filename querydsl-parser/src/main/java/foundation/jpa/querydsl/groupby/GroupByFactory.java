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
