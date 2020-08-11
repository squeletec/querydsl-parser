# querydsl-parser
[![Released version](https://img.shields.io/maven-central/v/foundation.fluent.api/querydsl-parser.svg)](https://search.maven.org/#search%7Cga%7C1%7Cquerydsl-parser)
[![Build Status](https://travis-ci.org/c0stra/querydsl-parser.svg?branch=master)](https://travis-ci.org/c0stra/querydsl-parser)

Language used to define JPA query using the JPA model, which under the hood uses Querydsl to construct the predicate.

The purpose is to provide SQL injection safe query language, which is flexible enough to build any queries, that the
excellent Java DSL tool Querydsl support.

__!!! Under development !!!__ This language is in initial development stage, so it supports very limited constructs.


## Maven configuration
Prerequisity is to have querydsl properly configured, and querydsl-apt generated entity query helpers.
In order to be able to use this parser use following maven dependency:

```xml
<dependency>
    <groupId>foundation.fluent.api</groupId>
    <artifactId>querydsl-parser</artifactId>
    <version>0.3</version>
</dependency>
```

## Quick example


```java
QueryContext context = QueryContext.createContext();

Predicate predicate = context.parse(QUser.user, "name = 'John Doe'");

Iterable<User> users = userRepository.findAll(predicate);
```
