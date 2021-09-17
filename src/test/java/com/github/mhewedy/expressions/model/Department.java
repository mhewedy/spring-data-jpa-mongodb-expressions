package com.github.mhewedy.expressions.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @GeneratedValue
    public Long id;
    public String name;
    @OneToOne(cascade = CascadeType.PERSIST)
    public City city;
}
