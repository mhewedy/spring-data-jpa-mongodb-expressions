package com.github.mhewedy.expressions;

import com.github.mhewedy.expressions.model.Employee;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends ExpressionsRepository<Employee, Long> {
}
