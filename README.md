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

Special thanks for [Rashad Saif](https://github.com/rashadsaif) and Hamada Elnoby for helping in the design and doing review for the code.  
