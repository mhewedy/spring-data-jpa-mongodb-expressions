# Spring Data JPA MongoDB Expressions

[![Java CI with Maven](https://github.com/mhewedy/spring-data-jpa-mongodb-expressions/actions/workflows/maven.yml/badge.svg)](https://github.com/mhewedy/spring-data-jpa-mongodb-expressions/actions/workflows/maven.yml) 
[![codecov](https://codecov.io/gh/mhewedy/spring-data-jpa-mongodb-expressions/branch/master/graph/badge.svg?token=3BR9MGYVC8)](https://codecov.io/gh/mhewedy/spring-data-jpa-mongodb-expressions)
[![javadoc](https://javadoc.io/badge2/com.github.mhewedy/spring-data-jpa-mongodb-expressions/javadoc.svg)](https://javadoc.io/doc/com.github.mhewedy/spring-data-jpa-mongodb-expressions) 
[![Join the chat at https://gitter.im/spring-data-jpa-mongodb-expressions/community](https://badges.gitter.im/spring-data-jpa-mongodb-expressions/community.svg)](https://gitter.im/spring-data-jpa-mongodb-expressions/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Tweet](https://img.shields.io/twitter/url/http/shields.io.svg?style=social)](https://twitter.com/intent/tweet?text=Use%20the%20MongoDB%20query%20syntax%20to%20query%20your%20relational%20database&url=https://github.com/mhewedy/spring-data-jpa-mongodb-expressions&via=Github&hashtags=java,springboot,mongodb,jpa,hibernate)

<image src="https://github.com/mhewedy/spring-data-jpa-mongodb-expressions/blob/master/logo.png?raw=true" style="display: block; margin: auto; width: 350px;">

### How it works:

1. Customize JPA Repository base class:
    ```java
    @SpringBootApplication
    @EnableJpaRepositories(repositoryBaseClass = ExpressionsRepositoryImpl.class)
    public class Application { … }
    ```
2. Change your repository to extend `ExpressionsRepository`:
    ```java
    @Repository
    public interface EmployeeRepository extends ExpressionsRepository<Employee, Long> {
    }
    ```
3. Build the controller/service:
    ```java
    @PostMapping("/search")
    public ResponseEntity<Page<EmployeeDto>> search(@RequestBody Expressions expressions, Pageable pageable) {

        return ok().body(
                    employeeRepository.findAll(expressions, pageable).map(employeeMapper::toDto)
            );
    }
    ```
4. Send [Mongodb query in JSON](https://mhewedy.github.io/spring-data-jpa-mongodb-expressions/#_how_to_build_the_expressions) from frontend:
    ```json
    {
      "$or": [
        {"lastName": "ibrahim"},
        {
          "$and": [
            {"firstName": "mostafa"},
            {"birthDate": {"$gt": "1990-01-01"}}
          ]
        }
      ]
    }
    ```

### Learn more

For a quick start see [this Medium post](https://mohewedy.medium.com/using-mongodb-query-syntax-to-query-relational-database-in-java-57701f0b0f0)
 or [dev.to post](https://dev.to/mhewedy/using-mongodb-query-syntax-to-query-relational-database-in-java-49hf)
 or see [this demo example on Github](https://github.com/springexamples/spring-data-jpa-mongodb-expressions-demo).

See [documentation website](https://mhewedy.github.io/spring-data-jpa-mongodb-expressions/) for details about how to get started.

### Install:

For spring-boot 3.x:
    
```xml
<dependency>
  <groupId>com.github.mhewedy</groupId>
  <artifactId>spring-data-jpa-mongodb-expressions</artifactId>
  <version>0.1.5</version>
</dependency>

```
For spring-boot 2.x:

```xml
<dependency>
  <groupId>com.github.mhewedy</groupId>
  <artifactId>spring-data-jpa-mongodb-expressions</artifactId>
  <version>0.0.8</version>
</dependency>

```

#### 🎖 Special Thanks 

Special thanks to [Rashad Saif](https://github.com/rashadsaif) and [Hamada Elnoby](https://github.com/hamadaelnopy) for helping in the design, inspring with ideas, and for doing code review.
    
#### In the News
This repo has been mentioned in [spring.io](http://spring.io/blog/2021/07/06/this-week-in-spring-july-6th-2021) weekly news.
