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

package foundation.jpa.querydsl.spring;

import foundation.jpa.querydsl.QueryContext;
import foundation.jpa.querydsl.QueryVariables;
import org.springframework.core.convert.ConversionService;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class JpaQueryContext extends QueryContext {

    public JpaQueryContext(ConversionService conversionService, EntityManager entityManager) {
        super(conversionService::convert);
    }

    public JpaQueryContext(EntityManager entityManager) {
        super((o, c) -> entityManager.find(c, o));
    }

    public static QueryVariables enumValues(EntityManager entityManager) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put(Integer.class.getSimpleName(), Integer.class);
        entityManager.getMetamodel().getEntities().stream().flatMap(e -> e.getAttributes().stream()).map(Attribute::getJavaType).filter(Class::isEnum)
                .peek(c -> variables.put(c.getSimpleName(), c))
                .flatMap(e -> Stream.of(((Class<Enum<?>>)e).getEnumConstants())).forEach(e -> variables.put(e.name(), e));
        return QueryVariables.map(variables);
    }

}
