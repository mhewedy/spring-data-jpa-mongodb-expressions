# Spring Data JPA MongoDB Expressions


[![Java CI with Maven](https://github.com/mhewedy/spring-data-jpa-mongodb-expressions/actions/workflows/maven.yml/badge.svg)](https://github.com/mhewedy/spring-data-jpa-mongodb-expressions/actions/workflows/maven.yml) [![codecov](https://codecov.io/gh/mhewedy/spring-data-jpa-mongodb-expressions/branch/master/graph/badge.svg?token=3BR9MGYVC8)](https://codecov.io/gh/mhewedy/spring-data-jpa-mongodb-expressions) [![javadoc](https://javadoc.io/badge2/com.github.mhewedy/spring-data-jpa-mongodb-expressions/javadoc.svg)](https://javadoc.io/doc/com.github.mhewedy/spring-data-jpa-mongodb-expressions)


Parses (a subset of) [MongoDB expressions](https://docs.mongodb.com/manual/tutorial/query-documents/) and convert them to Specifications to be used with Spring-Data-JPA 🎉.

## Why? 🤖
`spring-data-jpa-mongodb-expressions` allows you to **use the [MongoDB query syntax](https://docs.mongodb.com/manual/tutorial/query-documents/) to query your relational database.** This is specially useful to build dynamic search from the frontend app (a common case in business apps).

So, you can build the mongodb query-like json from the frontend app and pass it to the controller, and then optionally you enrich it with addtional conditions and pass it to the repository layer, in which the monogodb query will be translated automatically to JPA specification and executed.


## API 👾

This library provides an single interface `ExpressionsRepository` to be extended by your application repositories:

```java
public interface ExpressionsRepository<T, ID> extends JpaRepository<T, ID> {

    List<T> findAll(Expressions expressions);

    List<T> findAll(Expressions expressions, Sort sort);

    Page<T> findAll(Expressions expressions, Pageable pageable);
}
```
See [javadoc](https://javadoc.io/doc/com.github.mhewedy/spring-data-jpa-mongodb-expressions) for more information.

## How to start 🚀

1. You need to [customize](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.customize-base-repository) the base repository to be the `ExpressionsRepositoryImpl`.

```java
@Configuration
@EnableJpaRepositories(repositoryBaseClass = ExpressionsRepositoryImpl.class)
class ApplicationConfiguration { … }
```

2. Change the parent repository of your JPA repositories: 

```java
@Repository
public interface EmployeeRepository extends ExpressionsRepository<Employee, Long> {
}
```

3. Modify the search controller to accept `Expressions` in its parameter list:

```java
@PostMapping("/search")
public ResponseEntity<Page<EmployeeDto>> search(@RequestBody Expressions expressions, Pageable pageable) {

    // optional part
    expressions.and(Expression.of("departementId", $eq, getCurrentUserDeptId()));
    // add additional filters by ANDing or ORing more expression
    
    return ok().body(
                employeeRepository.findAll(expressions, pageable).map(employeeMapper::toDto)
        );
}
```

## Examples json expressions 🎭
The following is an example expressions that could be sent to the Controller Rest Apis, and will be deserialized into the `Expressions` object.

1-

Expression:
```json
{
  "lastName": "ibrahim",
  "$and": [
    {
      "birthDate": {"$gt": "1981-01-01"}
    },
    {
      "birthDate": {"$lte": "1985-10-10"}
    }
  ]
}
```

output:
```sql
... where last_name=? and birth_date>? and birth_date<=?
```

2-

Expression:
```json
{
  "$or": [
    {"lastName": "ibrahim"},
    {
      "$and": [
        {"firstName": "mostafa"},
        {
          "birthDate": {"$gt": "1990-01-01"}
        }
      ]
    }
  ]
}
```

output:
```sql
... where last_name = ? or first_name = ? and birth_date > ?
```

3-

Expression (joins):
```json
{
  "lastName": "ibrahim",
  "department.name": {"$contains":  "sw"}
}
```

output:
```sql
... from employee e inner join department d on e.department_id=d.id where e.last_name=? and d.name like ?
```

For a list of json queries, see :
1. the [resources](https://github.com/mhewedy/spring-data-jpa-mongodb-expressions/tree/master/src/test/resources) directory  
2. [ExpressionsRepositoryImplTest.java](https://github.com/mhewedy/spring-data-jpa-mongodb-expressions/blob/master/src/test/java/com/github/mhewedy/expressions/ExpressionsRepositoryImplTest.java)
3. [Mongodb docs](https://docs.mongodb.com/manual/tutorial/query-documents/) as a reference for the queries.

## Operators
The following is a list of supported [operators](https://github.com/mhewedy/spring-data-jpa-mongodb-expressions/blob/master/src/main/java/com/github/mhewedy/expressions/Operator.java):

Operator      | Description
----------- | -----------
$eq      | col = val   (if val is null then => col is null)
$ne     |  col <> val  (if val is null then => col is not null)
$ieq    |  lower(col) = lower(val)
$gt     |  col > val
$gte    |  col >= val
$lt     |  col < val
$lte    |  col <= val
$start  |  col like 'val%'
$end     |  col like '%val'
$contains|  col like '%val%'
$istart  |  lower(col) like 'lower(val)%'
$iend    |  lower(col) like '%lower(val)'
$icontains|  lower(col) like '%lower(val)%'
$in      |  col in (val1, val2, ...)
$nin     |  col not in (val1, val2, ...)
$or      |  expr1 or expr2
$and     |  expr1 and expr2

## Install

```xml
<dependency>
  <groupId>com.github.mhewedy</groupId>
  <artifactId>spring-data-jpa-mongodb-expressions</artifactId>
  <version>0.0.2</version>
</dependency>
```

## How to build the query on Frontend?
See this [snippet](https://playcode.io/753066/) to see how to build the query from js.

## Thanks 🎖

Special thanks to [Rashad Saif](https://github.com/rashadsaif) and Hamada Elnoby for helping in the design, inspring with ideas, and for doing the review for the code.  
