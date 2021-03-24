package com.github.mhewedy.expressions;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

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
    @ManyToOne(cascade = CascadeType.PERSIST)
    public Department department;
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name="employee_id")
    public List<Task> tasks;
}
