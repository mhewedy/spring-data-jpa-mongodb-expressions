# Spring data JPA MongoDB expressions

Parses (a subset of) MongoDB expressions and convert them to Specifications to be used to Spring-data-jpa project.

## Usage:

The usage of the library is simple. This library provides an single interface `ExpressionsRepository` to be extended by your application repositories, and it looks like:

```java
public interface ExpressionsRepository<T, ID> extends JpaRepository<T, ID> {

    List<T> findAll(Expressions expressions);

    List<T> findAll(Expressions expressions, Sort sort);

    Page<T> findAll(Expressions expressions, Pageable pageable);
}
```

To use the library, you will need to accept an object of type `Expressions` in your controller method:

```java
@PostMapping("/search")
public ResponseEntity<Page<EmployeeDto>> search(@RequestBody Expressions expressions, Pageable pageable) {

    expressions.and(Expression.of("departementId", $eq, getCurrentUserDeptId()));
    // add additional filters by ANDing or ORing more expression
    
    return ok()
            .body(employeeRepository.findAll(expressions, pageable)
            .map(employeeMapper::toDto)
            );
}
```

## Examples json expressions:
The following is an example expressions that could be sent to the Controller Rest Apis, and will be serialized into `Expressions` object.

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
    {
      "lastName": "ibrahim"
    },
    {
      "$and": [
        {
          "firstName": "mostafa"
        },
        {
          "birthDate": {
            "$gt": "1990-01-01"
          }
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

For a list of json queries, see :
1. the [resources](https://github.com/mhewedy/spring-data-jpa-mongodb-expressions/tree/master/src/test/resources) directory  
2. [ExpressionsRepositoryImplTest.java](https://github.com/mhewedy/spring-data-jpa-mongodb-expressions/blob/master/src/test/java/com/github/mhewedy/expressions/ExpressionsRepositoryImplTest.java)
3. [Mongodb docs](https://docs.mongodb.com/manual/tutorial/query-documents/) as a reference for the queries.

## Operators:
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

## Thanks:

Special thanks to [Rashad Saif](https://github.com/rashadsaif) and Hamada Elnoby for helping in the design and the inspring with ideas and for doing the review for the code.  
