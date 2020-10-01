# Querydsl parser
[![Released version](https://img.shields.io/maven-central/v/foundation.fluent.api/querydsl-parser.svg)](https://search.maven.org/#search%7Cga%7C1%7Cquerydsl-parser)
[![CI Build](https://travis-ci.com/c0stra/querydsl-parser.svg)](https://travis-ci.com/github/c0stra/querydsl-parser)

Yet another approach to allow users to query data modelled via JPA safely.

This project is simply adding a query language on top of the Querydsl flexible JPA querying engine.
It requires to have the Querydsl model classes generated using it's APT processor.

For more information about Querydsl, see http://www.querydsl.com/

The purpose of the language + parser is mostly to provide flexible querying of data to web applications.

__!!! Under development !!!__ This language is in initial development stage, so it supports very limited constructs.

## Quick start with spring boot
If you are looking for simple querying mechanism in your web application having data model represented by JPA, then
you can start very simply with following dependency:

```xml
<dependency>
    <groupId>foundation.fluent.api</groupId>
    <artifactId>spring-boot-jpa-search-starter</artifactId>
    <version>0.8</version>
</dependency>
```

This one will register parameter handlers for searching.

### Basic entities
You'll get 3 basic entities:

#### SearchCriteria<E extends EntityPath<?>>
Definition of criteria of a search. This is the argument, automatically resolved by Spring webmvc from user's parameters.
It aggregates together search condition written as text, and similarly sort criteria. Also it decodes teh Pageable for
offset and size of page.
When implementing the search - this is the type of the parameter to be used.
The generic parameter defines the specific Querydsl root entity.

#### SearchEngine
Search engine is a bean, that you have available, that can execute the search requests, and return the data.
It is not limited to any specific root entity, but can operate across all entities available in the current JPA
metamodel, as the `SearchCriteria<E>` is holding the information, what's the root entity for current search.

#### SearchResult<E>
Search result is a model of the whole result. It contains whole information about the `SearchCriteria<EntityPath<E>>`,
which was used to search for current result, and either the result data, or an error, that occurred during search execution.

Full example:
```java
@RestController
public class SearchController {

    private final SearchEngine searchEngine;

    public SearchController(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    @GetMapping("/search")
    public SearchResult<User> search(SearchCriteria<QUser> criteria) {
        return searchEngine.search(criteria);
    }

}
```

Running application, you can immediately start querying the data:

```
http://localhost:8080/search?query=name='John Doe'
```

#### Search<Q extends EntityPath<E>, E> shortcut
The previous approach still properly separates phases of invoking the controller with parameters, and retrieving the
model, potentially passing it to view.
At the end it may be quite repetitive, and one has still to get explicit dependencies on the SearchEngine bean.
The spring integration comes with a shortcut, which solves the whole search during parameter resolution.
It's extension of the `SearchResult<E>`, allowing to use it already as the resolvable parameter. Then such a simplified
controller can look like this:

```java
@RestController
public class SearchController {

    @GetMapping("/search")
    public SearchResult<User> search(Search<QUser, User> result) {
        return result;
    }
}
```

###
## Query language reference
Prerequisity is to have querydsl properly configured, and querydsl-apt generated entity query helpers.
In order to be able to use this parser use following maven dependency:

```xml
<dependency>
    <groupId>foundation.fluent.api</groupId>
    <artifactId>querydsl-parser</artifactId>
    <version>0.8</version>
</dependency>
```

|Values|   |   |
|---|---|---|
|Root entity| `project` |   |
|Root entity property access | `owner` |  |
|Root entity property access via root entity | `project.owner` | |
|Property access | `owner.name` | |
|Literals | `'John Doe'`, `1` | |
|Enums |`Status.OPEN`, `OPEN` | |
|One-to-many | `project.tasks.any` | |

|Operators|   |   |
|---|---|---|
|Equality| `project.name = 'MyProject'` | Can be used in any order  |
|Not equality | `project.name != 'MyProject'` |  |
|Entity resolution | `project.owner = 'admin'` | Given that user unique ID is string, and `owner` is many-to-one, this will query for user by it's id, and compare with the `owner` |
|Nullity | `is null` /  `not null` | |
|Sets | `project.name in ('MyProject', 'OtherProject')` / `project.name not in ('MyProject', 'OtherProject')` | |
|Boolean operators | `and`, `or` | |
|Negation | `not()` | |
|Relational operators | `>`, `<`, `>=`, `<=` | |
|Arithmetic operators | `+`, `-`, `*`, `/`, `%` | |
|Unary minus | `-owner.age` | |

|Function calls | | |
|---|---|---|
|Any function without parameters can be called as property | `tasks.any` | In fact this invokes Querydsl `any()` |
|Any function on the Java Querydsl entity is called with standard notation| `tasks.any()` | |

### Examples

```
project.tasks.any.state=OPEN
```


### Querydsl language extensions
As the language is in fact delegating method calls to the querydsl entity, any extension can be achieved by
adding Querydsl extensions.

Querydsl itself is very flexible and allows implementing extensions using `@QueryDelegate` methods.
See:
```
http://www.querydsl.com/static/querydsl/latest/reference/html/ch03s03.html#d0e2474
```
