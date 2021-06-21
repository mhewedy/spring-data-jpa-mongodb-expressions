package com.github.mhewedy.expressions.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Employee extends Auditable {
    @Id
    @GeneratedValue
    public Long id;

    public String firstName;
    public String lastName;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "ar", column = @Column(name = "employee_name_ar")),
            @AttributeOverride(name = "en", column = @Column(name = "employee_name_en"))
    })
    public LingualString name;
    public LocalDate birthDate;
    public Integer age;
    public Instant hireDate;
    public Short type;
    public Boolean active;
    @ManyToOne(cascade = CascadeType.PERSIST)
    public Department department;
    @OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    public List<Task> tasks;
}
