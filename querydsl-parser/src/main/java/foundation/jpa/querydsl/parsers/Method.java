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

import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import foundation.jpa.querydsl.EntityConverter;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.IntStream;

class Method implements Entry {

    private final java.lang.reflect.Method method;
    private final Object object;

    Method(java.lang.reflect.Method method, Object object) {
        this.method = method;
        this.object = object;
    }

    @Override
    public Expression<?> get() {
        throw new IllegalArgumentException();
    }

    private static Object[] arguments(List<Expression<?>> parameters) {
        return parameters.stream().map(p -> p instanceof Constant ? ((Constant<?>) p).getConstant() : p).toArray();
    }

    private Object[] resolveArgs(Object[] arguments, Class<?>[] types, EntityConverter context) {
        return IntStream.range(0, arguments.length).mapToObj(i -> context.convert(arguments[i], types[i])).toArray();
    }

    @Override
    public Expression<?> call(List<Expression<?>> parameters, EntityConverter entityConverter) {
        try {
            return Entry.toExpression(method.invoke(object, resolveArgs(arguments(parameters), method.getParameterTypes(), entityConverter)));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
