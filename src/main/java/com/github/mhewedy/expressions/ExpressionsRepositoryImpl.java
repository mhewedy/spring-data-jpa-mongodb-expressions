package com.github.mhewedy.expressions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;

@Slf4j
public class ExpressionsRepositoryImpl<T, ID>
        extends SimpleJpaRepository<T, ID> implements ExpressionsRepository<T, ID> {

    public ExpressionsRepositoryImpl(JpaEntityInformation<T, Long>
                                             entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }

    @Override
    public List<T> findAll(Expressions expressions) {
        return findAll(new ExpressionsSpecification<>(expressions));
    }

    @Override
    public List<T> findAll(Expressions expressions, Sort sort) {
        return findAll(new ExpressionsSpecification<>(expressions), sort);
    }

    @Override
    public Page<T> findAll(Expressions expressions, Pageable pageable) {
        return findAll(new ExpressionsSpecification<>(expressions), pageable);
    }

    @Override
    public long count(Expressions expressions) {
        return count(new ExpressionsSpecification<>(expressions));
    }

    @RequiredArgsConstructor
    private static class ExpressionsSpecification<T> implements Specification<T> {

        private final Expressions expressions;

        @Override
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            return ExpressionsPredicateBuilder.getPredicate(root, query, cb, expressions);
        }
    }
}
