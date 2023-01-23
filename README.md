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
2. Change your repository to extends `ExpressionsRepository`:
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
4. Send Mongodb query in JSON from frontend:
    <table><tr><td>
![image](https://user-images.githubusercontent.com/1086049/142768436-a218d0f6-4993-4361-af01-df62ad2774c4.png)
        </td></tr></table>

### Learn more

For quick start see [this Medium post](https://mohewedy.medium.com/using-mongodb-query-syntax-to-query-relational-database-in-java-57701f0b0f0)
 or [dev.to post](https://dev.to/mhewedy/using-mongodb-query-syntax-to-query-relational-database-in-java-49hf)
 or see [this demo example on Github](https://github.com/springexamples/spring-data-jpa-mongodb-expressions-demo).

See [documentation website](https://mhewedy.github.io/spring-data-jpa-mongodb-expressions/) for details about how to get started.

### Install:
    
```xml
<dependency>
  <groupId>com.github.mhewedy</groupId>
  <artifactId>spring-data-jpa-mongodb-expressions</artifactId>
  <version>0.1.0</version>
</dependency>

```
>Note: Starting from version `0.1.0` the library supports springboot 3, to use the library with springboot 2 use versions `0.0.x` e.g. version `0.0.5`.

#### 🎖 Special Thanks 

Special thanks to [Rashad Saif](https://github.com/rashadsaif) and [Hamada Elnoby](https://github.com/hamadaelnopy) for helping in the design, inspring with ideas, and for doing code review.  
    
#### Next Release
See [List of issues](https://github.com/mhewedy/spring-data-jpa-mongodb-expressions/issues?q=is%3Aissue+milestone%3A0.0.6) to be shipped in the next release
    
#### In the News
This repo has mentioned in [spring.io](http://spring.io/blog/2021/07/06/this-week-in-spring-july-6th-2021) weekly news.

#### Online Validator
see https://expressions-validator.fly.dev/ to help validate expressions
