package com.github.mhewedy.expressions;

import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends ExpressionsRepository<Employee, Long> {
}
