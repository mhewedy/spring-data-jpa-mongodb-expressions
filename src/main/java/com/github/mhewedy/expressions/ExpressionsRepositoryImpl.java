package com.github.mhewedy.expressions;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.util.ClassUtils;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@Slf4j
public class ExpressionsRepositoryImpl<T, ID>
        extends SimpleJpaRepository<T, ID> implements ExpressionsRepository<T, ID> {

    private static Object OBJECT_MAPPER;
    private static final boolean OBJECT_MAPPER_PRESENT = ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper",
            ExpressionsRepositoryImpl.class.getClassLoader());

    static {
        if (OBJECT_MAPPER_PRESENT) {
            OBJECT_MAPPER = new ObjectMapper();
        }
    }

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
    static class ExpressionsSpecification<T> implements Specification<T> {

        private final Expressions expressions;

        @Override
        public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            logExpressions();
            return ExpressionsPredicateBuilder.getPredicate(root, query, cb, expressions);
        }

        @SneakyThrows
        private void logExpressions() {
            if (!log.isDebugEnabled()) {
                return;
            }
            log.debug("expressions: {}", OBJECT_MAPPER_PRESENT ? ((ObjectMapper) OBJECT_MAPPER).writeValueAsString(expressions) : expressions);
        }
    }
}
