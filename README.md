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

## Thanks:
Special thanks for [Rashad Saif](https://github.com/rashadsaif) and Hamada alnoby for helping 
in the design and doing review for the code.  