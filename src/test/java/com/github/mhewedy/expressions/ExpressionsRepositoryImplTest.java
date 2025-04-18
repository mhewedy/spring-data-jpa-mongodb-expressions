package com.github.mhewedy.expressions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mhewedy.expressions.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.chrono.HijrahDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.github.mhewedy.expressions.Operator.*;
import static com.github.mhewedy.expressions.model.Employee.Lang;
import static com.github.mhewedy.expressions.model.Status.ACTIVE;
import static com.github.mhewedy.expressions.model.Status.NOT_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatList;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

@Slf4j
@DataJpaTest
@ContextConfiguration(classes = {JpaRepositoriesAutoConfiguration.class, DataSourceAutoConfiguration.class})
@EntityScan("com.github.mhewedy.expressions")
@EnableJpaRepositories(repositoryBaseClass = ExpressionsRepositoryImpl.class, basePackages = "com.github.mhewedy.expressions")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class ExpressionsRepositoryImplTest {

    private List<Employee> allEmployees;

    @Autowired
    EntityManager entityManager;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    public void setup() {
        log.info("setting up");
        allEmployees = Arrays.asList(
                new Employee(null,
                        "ahmed",
                        "ibrahim",
                        new LingualString("ahmed ar", "ahmed en"),
                        LocalDate.of(1980, 10, 10),
                        HijrahDate.of(1380, 10, 10),
                        10,
                        Instant.parse("2007-12-03T10:15:30.00Z"),
                        (short) 1,
                        true,
                        new Department(null, "hr", new City(null, "cairo")),
                        Arrays.asList(new Task(null, "fix hr", ACTIVE), new Task(null, "fix hr", ACTIVE)),
                        UUID.fromString("2dfb7bc7-38a6-4826-b6d3-297969d17244"),
                        Lang.AR,
                        Lang.AR
                ),
                new Employee(null,
                        "mohammad",
                        "ibrahim",
                        new LingualString("mohammad ar", "mohammad en"),
                        LocalDate.of(1985, 10, 10),
                        HijrahDate.of(1385, 10, 10),
                        20,
                        Instant.parse("2009-12-03T10:15:30.00Z"),
                        (short) 1,
                        true,
                        new Department(null, "sw arch", null),
                        Arrays.asList(new Task(null, "fix sw arch", ACTIVE), new Task(null, "fix sw arch", ACTIVE)),
                        UUID.randomUUID(),
                        Lang.AR,
                        Lang.AR
                ),
                new Employee(null,
                        "mostafa",
                        "ahmed",
                        new LingualString("mostafa ar", "mostafa en"),
                        LocalDate.of(1988, 10, 10),
                        HijrahDate.of(1388, 10, 10),
                        30,
                        Instant.parse("2011-12-03T10:15:30.00Z"),
                        (short) 2,
                        true,
                        new Department(null, "sw dev", new City(null, "alex")),
                        Arrays.asList(new Task(null, "fix sw dev", ACTIVE), new Task(null, "fix sw dev", ACTIVE)),
                        UUID.randomUUID(),
                        Lang.AR,
                        Lang.AR
                ),
                new Employee(null,
                        "wael",
                        "ibrahim",
                        new LingualString("wael ar", "wael en"),
                        LocalDate.of(1990, 10, 10),
                        HijrahDate.of(1390, 10, 10),
                        40,
                        Instant.parse("2015-12-03T10:15:30.00Z"),
                        (short) 2,
                        true,
                        new Department(null, "hr", new City(null, "cairo")),
                        Arrays.asList(new Task(null, "fix hr", ACTIVE), new Task(null, "fix hr", ACTIVE)),
                        UUID.randomUUID(),
                        Lang.AR,
                        Lang.AR
                ),
                new Employee(null,
                        "farida",
                        "abdullah",
                        new LingualString("farida ar", "farida en"),
                        LocalDate.of(1979, 10, 10),
                        HijrahDate.of(1379, 10, 10),
                        50,
                        Instant.parse("2017-12-03T10:15:30.00Z"),
                        (short) 2,
                        false,
                        new Department(null, "hr", new City(null, "cairo")),
                        Arrays.asList(new Task(null, "fix hr", ACTIVE), new Task(null, "fix hr", NOT_ACTIVE)),
                        UUID.randomUUID(),
                        Lang.AR,
                        Lang.AR
                ),
                new Employee(null,
                        "fofo",
                        "bobo",
                        new LingualString("fofo ar", "fofo en"),
                        LocalDate.of(1979, 10, 10),
                        HijrahDate.of(1379, 10, 10),
                        50,
                        Instant.parse("2017-12-03T10:15:30.00Z"),
                        (short) 2,
                        false,
                        null,
                        null,
                        UUID.randomUUID(),
                        Lang.EN,
                        Lang.EN
                )
        );
        employeeRepository.saveAll(allEmployees);

        bookRepository.saveAll(List.of(
                new Book(new Book.BookId("Spring in Action", "English"), "Craig Walls"),
                new Book(new Book.BookId("Kubernetes Up and Running", "English"), "Brendan Burns")
        ));
    }

    @AfterEach
    public void finish() {
        log.info("cleaning");
        employeeRepository.deleteAll();
    }

    @Test
    public void testFindAllInBaseRepository() throws Exception {

        String json = loadResourceJsonFile("testFindAllInBaseRepository");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(1);
        assertThat(employeeList.get(0).firstName).isEqualTo("mohammad");

        // where last_name=? and birth_date>? and birth_date<=?
    }

    @Test
    public void testPagingAndSorting() throws Exception {

        String json = loadResourceJsonFile("testPagingAndSorting");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        Page<Employee> employeeList =
                employeeRepository.findAll(expressions, PageRequest.of(0, 3, Sort.by("firstName").descending()));
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.getTotalElements()).isEqualTo(6);
        assertThat(employeeList.getSize()).isEqualTo(3);
        assertThat(employeeList.getContent().get(0).firstName).isEqualTo("wael");
    }

    @Test
    public void testComplexCaseWithMultipleOrAndExpressions() throws Exception {

        String json = loadResourceJsonFile("testComplexCaseWithMultipleOrAndExpressions");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(3);
        // where last_name = ? or first_name = ? and birth_date > ?
    }

    @Test
    public void testComplexCaseWithMultipleOrAndExpressions2() throws Exception {

        String json = loadResourceJsonFile("testComplexCaseWithMultipleOrAndExpressions2");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(2);
        // where last_name = ? and (first_name = ? or birth_date < ?)
    }

    @Test
    public void testUsingTheBuilderMethodOfExpressionsWithOr() throws Exception {

        String json = loadResourceJsonFile("testUsingTheBuilderMethodOfExpressionsWithOr");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        expressions.and(Expression.or(
                Expression.of("lastName", $eq, "ibrahim"),
                Expression.of("age", $in, 10, 30)
        ));

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(3);

        // where hire_date<? and (last_name=? or age in (? , ?))
    }

    @Test
    public void testUsingTheBuilderMethodOfExpressionsWithAnd() throws Exception {

        String json = loadResourceJsonFile("testUsingTheBuilderMethodOfExpressionssWithAnd");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        expressions.or(Expression.and(
                Expression.of("lastName", $eq, "ibrahim"),
                Expression.of("age", $eq, 10)
        ));

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(3);

        // where hire_date<? or last_name=? and age=?
    }

    @Test
    public void testSearchByEmptyObject() throws Exception {

        String json = "{}";

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(6);

        // where ?=1
    }

    @Test
    public void testSearchByEmptyObjectAndWithDatesFromJava() throws Exception {

        String json = "{}";

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        expressions.and(Expression.of("birthDate", $eq, "1980-10-10"));
        expressions.and(Expression.of("hireDate", $lte, "2007-12-03T10:15:30.00Z"));

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(1);
        assertThat(employeeList.get(0).firstName).isEqualTo("ahmed");

        // where employee0_.hire_date=? and employee0_.birth_date=?
    }

    @Test
    public void testSearchByNull() throws Exception {

        String json = loadResourceJsonFile("testSearchByNull");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        expressions.or(Expression.of("lastName", $ne, (String) null));

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(6);

        // where employee0_.first_name is null or employee0_.last_name is not null
    }

    @Test
    public void testSearchByContains() throws Exception {

        String json = loadResourceJsonFile("testSearchByContains");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(3);

        // where employee0_.last_name like ?
    }

    @Test
    public void testSearchByIgnoreCaseContains() throws Exception {

        String json = loadResourceJsonFile("testSearchByIgnoreCaseContains");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(3);

        // where lower(employee0_.last_name) like ?
    }

    @Test
    public void testOperatorNotSupported() throws Exception {

        String json = loadResourceJsonFile("testOperatorNotSupported");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        try {
            employeeRepository.findAll(expressions);

            fail("should throw exception");
        } catch (Exception ex) {
            assertThat(ex.getMessage())
                    .contains("No enum constant")
                    .contains("Operator.$not_supported_operator");
        }
    }

    @Test
    public void testInvalidFieldName() throws Exception {

        String json = loadResourceJsonFile("testInvalidFieldName");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        try {
            employeeRepository.findAll(expressions);

            fail("should throw exception");
        } catch (Exception ex) {
            assertThat(ex.getMessage())
                    .contains("invalidFieldName")
                    .contains("com.github.mhewedy.expressions.model.Employee");
        }
    }

    @Test
    public void testTheJavaAPI() {
        Expressions expressions = Expression.of("lastName", $eq, "ibrahim")
                .and(Expression.or(
                        Expression.of("age", $in, 10, 20),
                        Expression.of("birthDate", $lt, LocalDate.of(1980, 1, 1)))
                ).build();

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(2);

        // where last_name=? and (age in (? , ?) or birth_date<?)
    }

    @Test
    public void testSearchUsingInOperatorInNonIntegerNumericField() throws Exception {
        String json = loadResourceJsonFile("testSearchUsingInOperatorInNonIntegerNumericField");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(6);

        // where type in ? or active=?
    }

    @Test
    public void testNestingUsingManyToOneJoin() throws Exception {
        String json = loadResourceJsonFile("testNestingUsingManyToOneJoin");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(1);

        // from employee e inner join department d on e.department_id=d.id where e.last_name=? and (d.name like ?)
    }

    @Test
    public void testNestingUsingManyToOneJoinUsingInQueries() throws Exception {
        String json = loadResourceJsonFile("testNestingUsingManyToOneJoinUsingInQueries");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(4);

        // employee e inner join department d on e.department_id=d.id where d.name in (? , ?)
    }

    @Test
    public void testNestingUsingManyToOneJoinUsingDeepNestedLevel() throws Exception {
        String json = loadResourceJsonFile("testNestingUsingManyToOneJoinUsingDeepNestedLevel");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(2);

        // from employee e inner join department d on e.department_id=d.id inner join city c on d.city_id=c.id where e.last_name=? and c.name=?
    }

    @Test
    public void testNestingUsingOneToManyJoin() throws Exception {
        String json = loadResourceJsonFile("testNestingUsingOneToManyJoin");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(5);

        // from employee e inner join task t on e.id=t.employee_id where t.name like ?
    }

    @Test
    public void testNestingUsingManyToOneJoinWithMultipleFields() throws Exception {
        String json = loadResourceJsonFile("testNestingUsingManyToOneJoinWithMultipleFields");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(0);

        // from employee e inner join department d on e.department_id=d.id where e.last_name=? and d.id=? and d.name=?
    }

    @Test
    public void testNestingUsingManyToOneJoinWithMultipleFields_Advanced() throws Exception {
        String json = loadResourceJsonFile("testNestingUsingManyToOneJoinWithMultipleFields_Advanced");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(2);

        // from employee e inner join department d on e.department_id=d.id inner join city c on d.city_id=c.id
        // where e.last_name=? and c.name=? and (d.name in (? , ?))
    }

    @Test
    public void testEmbeddedAndJoin() throws Exception {
        String json = loadResourceJsonFile("testEmbeddedAndJoin");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(1);

        // from employee e inner join department d on e.department_id=d.id where e.employee_name_ar=? and d.name=?
    }

    @Test
    public void testEmbeddedInAndJoin() throws Exception {
        String json = loadResourceJsonFile("testEmbeddedInAndJoin");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(2);

        // from employee e inner join department d on e.department_id=d.id where (e.employee_name_ar in (? , ?)) and d.name=?
    }

    @Test
    public void testNumberContains() throws Exception {
        String json = loadResourceJsonFile("testNumberContains");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(6);

        // from employee e where cast(e.age as varchar(255)) like ?
    }

    @Test
    public void testNumberContains_Count() throws Exception {
        String json = loadResourceJsonFile("testNumberContains");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        long count = employeeRepository.count(expressions);
        assertThat(count).isEqualTo(6);

        // from employee e where cast(e.age as varchar(255)) like ?
    }

    @Test
    public void testEnumInInts() throws Exception {
        String json = loadResourceJsonFile("testEnumInInts");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(5);

        // from employee e inner join task t on e.id=t.employee_id where t.status in (? , ?)
    }

    @Test
    public void testEnumNotInStrings() throws Exception {
        String json = loadResourceJsonFile("testEnumNotInStrings");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(1);

        // from employee e inner join task t on e.id=t.employee_id where t.status not in (?)
    }

    @Test
    public void testManyToOneIsNull() throws Exception {
        String json = loadResourceJsonFile("testManyToOneIsNull");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(1);

        // from employee e where e.department is null
    }

    @Test
    public void testBooleanOperatorFromJava() {
        Expressions expressions = Expression.of("active", $eq, false).build();

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(2);

        // where e.active=?
    }

    @Test
    public void testUUID() throws Exception {
        String json = loadResourceJsonFile("testUUID");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(1);

        // where e.serial=?
    }

    @Test
    public void testHijrahDate() throws Exception {
        String json = loadResourceJsonFile("testHijrahDate");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(1);

        // where e.h_birth_date>=?
    }

    @Test
    public void testHijrahDate_InJava() {
        Expressions expressions = Expression.of("hBirthDate", $gte, HijrahDate.of(1390, 9, 29)).build();

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(1);

        // where e.h_birth_date>=?
    }

    @Test
    public void testCompositeIdUsingEmbeddable() throws Exception {
        String json = loadResourceJsonFile("testCompositeIdUsingEmbeddable");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Book> employeeList = bookRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(1);

        // where b1_0.auther=?
    }

    @Test
    public void testCompositeIdUsingEmbeddable_QueryByIdParts() throws Exception {
        String json = loadResourceJsonFile("testCompositeIdUsingEmbeddable_QueryByIdParts");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Book> employeeList = bookRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(1);

        // where b1_0.title=?
    }

    @Test
    public void testCompositeIdUsingEmbeddable_QueryByIdParts2() throws Exception {
        String json = loadResourceJsonFile("testCompositeIdUsingEmbeddable_QueryByIdParts2");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Book> employeeList = bookRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(2);

        // where b1_0.language=?
    }

    @Test
    public void testAggregations_by_convert_to_specifications_InJava() {
        Expressions expressions = Expression.of("hBirthDate", Operator.$gte, HijrahDate.of(1388, 9, 29)).build();

        // get spring-data-jpa Specification from the expressions object
        Specification<Employee> specification = expressions.getSpecification();

        // then using old school jpa Criteria Query API
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
        Root<Employee> root = query.from(Employee.class);
        query.select(cb.min(root.get("age"))).where(specification.toPredicate(root, query, cb));
        Integer minAge = entityManager.createQuery(query).getSingleResult();

        assertThat(minAge).isEqualTo(30);

        // select min(e1_0.age) from employee e1_0 where e1_0.h_birth_date>=?
    }

    @Test
    public void testAndingMultipleOrs_InJava() {

        Expressions expressions = new Expressions();

        expressions.and(Expression.or(
                Expression.of("age", $eq, 30),
                Expression.of("age", $eq, 50)
        ));

        expressions.and(Expression.or(
                Expression.of("firstName", $eq, "ali"),
                Expression.of("firstName", $eq, "wael")
        ));

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(0);

        // where (employee0_.age=? or employee0_.age=?) and (employee0_.first_name=? or employee0_.first_name=?)
    }

    @Test
    public void testEnum1() {
        Expressions expressions = new Expressions();
        expressions.and(Expression.of("lang", $eq, Lang.EN));
        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(1);
    }

    @Test
    public void testEnum2() {
        Expressions expressions = new Expressions();
        expressions.and(Expression.of("langCode", $eq, Lang.EN));
        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(1);
    }

    @Test
    public void testEnum3() {
        Expressions expressions = new Expressions();
        expressions.and(Expression.of("langCode", $in, Lang.EN, Lang.AR));
        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(allEmployees.size());
    }

    @Test
    public void testTheList() {
        Expressions expressions = Expression.of("lastName", $eq, "ibrahim")
                .and(Expression.or(
                        Expression.of("age", $in, Arrays.asList(10, 20)),
                        Expression.of("birthDate", $lt, LocalDate.of(1980, 1, 1)))
                ).build();

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList).isNotNull();
        assertThat(employeeList.size()).isEqualTo(2);

        // where last_name=? and (age in (? , ?) or birth_date<?)
    }

    @Test
    public void testLeftJoin() throws JsonProcessingException {
        String json = loadResourceJsonFile("testLeftJoin");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(3);

        // ...from employee e join department d on d.id = e.department_id left join city c on c.id = d.city_id where e.first_name = ? or c.name = ?
    }

    @Test
    public void testLeftJoinAlternateSyntax() throws JsonProcessingException {
        String json = loadResourceJsonFile("testLeftJoinAlternateSyntax");

        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);

        List<Employee> employeeList = employeeRepository.findAll(expressions);
        assertThat(employeeList.size()).isEqualTo(3);

        // ...from employee e join department d on d.id = e.department_id left join city c on c.id = d.city_id where e.first_name = ? or c.name = ?
    }

    @Test
    public void testSearchWithMultipleNestedJoins() throws Exception {
        String json = """
                {
                    "$and": [
                        {
                            "department.city.name": "cairo"
                        },
                        {
                            "tasks.status": "ACTIVE"
                        }
                    ]
                }
                """;
        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);
        List<Employee> employees = employeeRepository.findAll(expressions);

        assertThat(employees).isNotNull();
        assertThatList(employees).hasSize(3); // All employees in Cairo with active tasks
        assertThatList(employees).allSatisfy(emp -> {
            assertThat(emp.department).isNotNull();
            assertThat(emp.department.city).isNotNull();
            assertThat(emp.department.city.name).isEqualTo("cairo");
            assertThat(emp.tasks).isNotNull();
            assertThat(emp.tasks.stream().anyMatch(task -> task.status == ACTIVE)).isTrue();
        });
    }

    @Test
    public void testSearchWithComplexNestedConditions() throws Exception {
        String json = """
                {
                    "$or": [
                        {
                            "department.name": "hr",
                            "tasks.status": "ACTIVE"
                        },
                        {
                            "department.name": "sw dev",
                            "age": { "$gt": 25 }
                        }
                    ]
                }
                """;
        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);
        List<Employee> employees = employeeRepository.findAll(expressions);

        assertThat(employees).isNotNull();
        assertThatList(employees).hasSize(4); // HR employees with active tasks + SW dev employees over 25
        assertThatList(employees).allSatisfy(emp -> {
            if (emp.department != null && emp.department.name.equals("hr")) {
                assertThat(emp.tasks).isNotNull();
                assertThat(emp.tasks.stream().anyMatch(task -> task.status == ACTIVE)).isTrue();
            } else if (emp.department != null && emp.department.name.equals("sw dev")) {
                assertThat(emp.age).isGreaterThan(25);
            }
        });
    }

    @Test
    public void testSearchWithMultipleSortOrders() throws Exception {
        String json = """
                {
                    "department.name": "hr"
                }
                """;
        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);
        Sort sort = Sort.by(
            Sort.Order.asc("firstName"),
            Sort.Order.desc("age")
        );
        List<Employee> employees = employeeRepository.findAll(expressions, sort);

        assertThat(employees).isNotNull();
        assertThatList(employees).hasSize(3); // All HR employees
        assertThatList(employees).allSatisfy(emp -> {
            assertThat(emp.department).isNotNull();
            assertThat(emp.department.name).isEqualTo("hr");
        });
        
        // Verify sorting
        for (int i = 1; i < employees.size(); i++) {
            Employee prev = employees.get(i-1);
            Employee curr = employees.get(i);
            assertThat(prev.firstName.compareTo(curr.firstName)).isLessThanOrEqualTo(0);
            if (prev.firstName.equals(curr.firstName)) {
                assertThat(prev.age).isGreaterThanOrEqualTo(curr.age);
            }
        }
    }

    @Test
    public void testSearchWithPaginationAndComplexConditions() throws Exception {
        String json = """
                {
                    "$or": [
                        {
                            "department.name": "hr"
                        },
                        {
                            "department.name": "sw dev"
                        }
                    ],
                    "age": { "$gte": 30 }
                }
                """;
        Expressions expressions = new ObjectMapper().readValue(json, Expressions.class);
        Page<Employee> page = employeeRepository.findAll(expressions, PageRequest.of(0, 2, Sort.by("firstName")));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isEqualTo(3); // Total matching employees
        assertThat(page.getSize()).isEqualTo(2); // Page size
        assertThat(page.getContent()).isNotNull();
        assertThatList(page.getContent()).hasSize(2); // Current page content
        assertThatList(page.getContent()).allSatisfy(emp -> {
            assertThat(emp.department).isNotNull();
            assertThat(emp.department.name).isIn("hr", "sw dev");
            assertThat(emp.age).isGreaterThanOrEqualTo(30);
        });
    }

    @SneakyThrows
    private String loadResourceJsonFile(String name) {
        File file = ResourceUtils.getFile("classpath:" + name + ".json");
        return new String(Files.readAllBytes(file.toPath()));
    }
}
