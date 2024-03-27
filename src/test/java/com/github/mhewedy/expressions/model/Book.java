package com.github.mhewedy.expressions.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @EmbeddedId
    public BookId id;

    public String auther;

    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookId implements Serializable {
        public String title;
        public String language;
    }
}
