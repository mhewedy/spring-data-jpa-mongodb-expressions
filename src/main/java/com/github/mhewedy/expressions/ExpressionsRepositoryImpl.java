package com.github.mhewedy.expressions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
        ExpressionsSpecification<T> specifications = new ExpressionsSpecification<>(expressions);
        return findAll(specifications);
    }

    @Override
    public List<T> findAll(Expressions expressions, Sort sort) {
        ExpressionsSpecification<T> specifications = new ExpressionsSpecification<>(expressions);
        return findAll(specifications, sort);
    }

    @Override
    public Page<T> findAll(Expressions expressions, Pageable pageable) {
        ExpressionsSpecification<T> specifications = new ExpressionsSpecification<>(expressions);
        return findAll(specifications, pageable);
    }

    @RequiredArgsConstructor
    private static class ExpressionsSpecification<T> implements Specification<T> {

        private final Expressions expressions;

        @Override
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            query.distinct(true);   // to eliminate duplicate in case of one-to-many and many-to-many associations
            return ExpressionsPredicateBuilder.getPredicate(root, cb, expressions);
        }
    }
}
