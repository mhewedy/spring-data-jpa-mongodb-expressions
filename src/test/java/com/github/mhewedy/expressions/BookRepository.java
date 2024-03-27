package com.github.mhewedy.expressions;

import com.github.mhewedy.expressions.model.Book;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends ExpressionsRepository<Book, Book.BookId> {
}
