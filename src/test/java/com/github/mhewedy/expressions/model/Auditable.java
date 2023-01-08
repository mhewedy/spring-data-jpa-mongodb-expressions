package com.github.mhewedy.expressions.model;

import jakarta.persistence.MappedSuperclass;
import java.time.Instant;

@MappedSuperclass
public class Auditable {

    public Instant createdDate;
}
