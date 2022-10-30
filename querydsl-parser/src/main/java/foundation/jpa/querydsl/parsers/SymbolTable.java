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

package foundation.jpa.querydsl.parsers;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class SymbolTable {

    private final Map<String, Entry> entryMap = new HashMap<>();

    private java.util.function.Function<String, Entry> dynamicMap = name -> {
        throw new IllegalStateException("No such property: " + name + ". Available properties are: " + entryMap.values());
    };

    public Entry get(String name) {
        return entryMap.containsKey(name) ? entryMap.get(name) : dynamicMap.apply(name);
    }

    public SymbolTable set(String name, Entry entry) {
        entryMap.put(name, entry);
        return this;
    }

    public SymbolTable setRoot(EntityPath<?> entityPath) {
        Stream.of(entityPath.getClass().getFields()).forEach(field -> {
            try {
                set(field.getName(), value(field.get(entityPath)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        Stream.of(entityPath.getClass().getMethods()).filter(method -> !method.getDeclaringClass().equals(Object.class)).forEach(method -> {
            if(method.getName().equals("get") && method.getParameterCount() == 1) {
                dynamicMap = new DynamicMap(entityPath, method);
            } else if(method.getName().startsWith("get") && method.getParameterCount() == 0) {
                set(uncap(method.getName().substring(3)), new Property(method, entityPath));
                set(method.getName(), new Method(method, entityPath));
            } else if(method.getParameterCount() == 0) {
                set(method.getName(), new Property(method, entityPath));
            } else {
                set(method.getName(), new Method(method, entityPath));
            }
            set(method.getName(), new Function());
        });
        return this;
    }

    static private String uncap(String string) {
        return string.substring(0,1).toLowerCase() + string.substring(1);
    }

    public static Entry value(Object object) {
        return object instanceof EntityPath ? new Path((EntityPath<?>) object) : new Variable(object);
    }

    public Entry access(Class<?> c, Expression<?> target, String name) {
        return new SymbolTable().setRoot(target).get(name);
    }
}
