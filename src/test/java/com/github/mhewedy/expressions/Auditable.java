package com.github.mhewedy.expressions;

import javax.persistence.MappedSuperclass;
import java.time.Instant;

@MappedSuperclass
public class Auditable {

    public Instant createdDate;
}
