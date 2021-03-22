package com.github.mhewedy.expressions;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    @Id
    @GeneratedValue
    public Long id;

    public String firstName;
    public String lastName;
    public LocalDate birthDate;
    public Integer age;
    public Instant hireDate;
    public Short type;
    public Boolean active;
}
