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

package foundation.jpa.querydsl.spring.testapp;

import foundation.jpa.querydsl.QueryVariables;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.inject.Named;
import javax.persistence.EntityManager;

import java.util.Collections;

import static foundation.jpa.querydsl.spring.JpaQueryContext.enumValues;
import static java.util.Arrays.asList;

@SpringBootApplication
public class SearchApplication implements WebMvcConfigurer {

    public static void main(String... args) {
        SpringApplication.run(SearchApplication.class);
    }

    @Bean
    @Named("global")
    public QueryVariables globalVariables(EntityManager entityManager) {
        return enumValues(entityManager);
    }

    @Primary
    @Bean
    @Scope(WebApplicationContext.SCOPE_SESSION)
    public QueryVariables sessionVariables(@Named("global") QueryVariables globalVariables) {
        return QueryVariables.local(Collections.singletonMap("local", "value"), globalVariables);
    }

    @Bean
    public boolean data(RootEntityRepository repository) {
        repository.save(new RootEntity().setName("ROOT1").setEnumValue(EnumValue.VALUE1).setSize(15).setManyToOneEntity(new ManyToOneEntity()).setManyToManyEntities(asList(
                new ManyToManyEntity(), new ManyToManyEntity()
        )).setOneToManyEntities(asList(
                new OneToManyEntity().setString("A"), new OneToManyEntity().setString("B"), new OneToManyEntity().setString("C")
        )));
        repository.save(new RootEntity().setName("ROOT2").setEnumValue(EnumValue.VALUE2).setSize(0).setManyToOneEntity(new ManyToOneEntity()).setManyToManyEntities(asList(
                new ManyToManyEntity(), new ManyToManyEntity()
        )).setOneToManyEntities(asList(
                new OneToManyEntity().setString("D"), new OneToManyEntity().setString("A")
        )));
        return true;
    }

}
