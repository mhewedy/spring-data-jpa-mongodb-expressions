package com.github.mhewedy.expressions;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface ExpressionsRepository<T, ID> extends JpaRepository<T, ID> {

    List<T> findAll(Expressions expressions);

    List<T> findAll(Expressions expressions, Sort sort);

    Page<T> findAll(Expressions expressions, Pageable pageable);
}
