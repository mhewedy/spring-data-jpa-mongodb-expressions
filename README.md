# Spring data JPA Mongodb expressions

Parses (subset of) mongodb expressions and convert it to Specifications to be used to Spring-data-jpa project. 

## Examples:

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

## Operators:
The following is lis of supported [operators](https://github.com/mhewedy/spring-data-jpa-mongodb-expressions/blob/master/src/main/java/com/github/mhewedy/expressions/Operator.java):
```
$eq(false),          // col = val   (if val is null then => col is null)
$ne(false),          // col <> val  (if val is null then => col is not null)
$ieq(false),         // lower(col) = lower(val)

$gt(false),          // col > val
$gte(false),         // col >= val
$lt(false),          // col < val
$lte(false),         // col <= val

$start(false),       // col like 'val%'
$end(false),         // col like '%val'
$contains(false),    // col like '%val%'
$istart(false),      // lower(col) like 'lower(val)%'
$iend(false),        // lower(col) like '%lower(val)'
$icontains(false),   // lower(col) like '%lower(val)%'

$in(true),           // col in (val1, val2, ...)
$nin(true),          // col not in (val1, val2, ...)

$or(false),          // expr1 or expr2
$and(false);         // expr1 and expr2
```

## Thanks:
Special thanks for [Rashad Saif](https://github.com/rashadsaif) and Hamada alnoby for helping 
in the design and doing review for the code.  