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

package foundation.jpa.querydsl;

import com.querydsl.core.types.dsl.Expressions;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public interface PropertyResolver {

    Object access(Class<?> aClass, String name, Object instance);

    static PropertyResolver defaultResolver(QueryVariables variables) {
        return (aClass, name, instance) -> {
            for(Access access : Arrays.<Access>asList(
                    () -> aClass.getField(name).get(instance),
                    () -> aClass.getMethod("get", String.class).invoke(instance, name),
                    () -> aClass.getMethod("get", Object.class).invoke(instance, name),
                    () -> aClass.getMethod(name).invoke(instance),
                    () -> aClass.getMethod("get" + name.substring(0,1).toUpperCase() + name.substring(1)).invoke(instance),
                    () -> {
                        if(variables.isDefined(name)) {
                            Object key = variables.get(name);
                            return aClass.getMethod("get", key.getClass()).invoke(instance, key);
                        }
                        throw new NoSuchMethodException("No such method");
                    },
                    () -> Expressions.class.getMethod(name).invoke(null)
            )) try {
                return access.get();
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e.getMessage() + " on " + instance);
            } catch (NoSuchFieldException | NoSuchMethodException e) {
                // Try next
            }
            throw new RuntimeException("No such property: " + name + " on " + instance + ". Available properties are: " + Stream.of(aClass.getFields()).map(Field::getName).collect(joining(", ")));
            //+ concat(of(c.getFields()).map(Field::getName), of(c.getMethods()).filter(m -> m.getParameterCount() == 0).map(Method::getName)).collect(joining(", ")));
        };
    }

    interface Access {
        Object get() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException;
    }

}
