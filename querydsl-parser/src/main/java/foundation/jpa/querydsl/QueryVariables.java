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

package foundation.jpa.querydsl;

import java.util.Map;

public interface QueryVariables {

    Object get(String name);

    boolean isDefined(String name);

    <T> T define(String name, T value);

    static QueryVariables none() {
        return new QueryVariables() {
            @Override
            public Object get(String name) {
                return null;
            }

            @Override
            public boolean isDefined(String name) {
                return false;
            }

            @Override
            public <T> T define(String name, T value) {
                return value;
            }
        };
    }

    static QueryVariables map(Map<String, Object> values) {
        return new QueryVariables() {
            @Override
            public Object get(String name) {
                return values.get(name);
            }

            @Override
            public boolean isDefined(String name) {
                return values.containsKey(name);
            }

            @Override
            public <T> T define(String name, T value) {
                values.put(name, value);
                return value;
            }
        };
    }

    static QueryVariables local(Map<String, Object> values, QueryVariables parent) {
        return new QueryVariables() {
            @Override
            public Object get(String name) {
                return values.containsKey(name) ? values.get(name) : parent.get(name);
            }

            @Override
            public boolean isDefined(String name) {
                return values.containsKey(name) || parent.isDefined(name);
            }

            @Override
            public <T> T define(String name, T value) {
                values.put(name, value);
                return value;
            }
        };
    }

}
