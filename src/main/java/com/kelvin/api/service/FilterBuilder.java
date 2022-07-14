package com.kelvin.api.service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public abstract class FilterBuilder<T> extends HttpBase{

    public void buildIntegerFieldFilters(String name, CriteriaBuilder criteriaBuilder, Root<T> root, List<Predicate> predicates){

        if (nn("eq." + name)) {
            predicates.add(criteriaBuilder.equal(root.get(name), _integer("eq." + name)));
        }
        if (nn("gt." + name)) {
            predicates.add(criteriaBuilder.gt(root.get(name), _integer("gt." + name)));
        }
        if (nn("ge." + name)) {
            predicates.add(criteriaBuilder.ge(root.get(name), _integer("ge." + name)));
        }
        if (nn("lt." + name)) {
            predicates.add(criteriaBuilder.lt(root.get(name), _integer("lt." + name)));
        }
        if (nn("le." + name)) {
            predicates.add(criteriaBuilder.le(root.get(name), _integer("le." + name)));
        }
    }

    public void buildDoubleFieldFilters(String name, CriteriaBuilder criteriaBuilder, Root<T> root, List<Predicate> predicates){

        if (nn("eq." + name)) {
            predicates.add(criteriaBuilder.equal(root.get(name), _double("eq." + name)));
        }
        if (nn("gt." + name)) {
            predicates.add(criteriaBuilder.gt(root.get(name), _double("gt." + name)));
        }
        if (nn("ge." + name)) {
            predicates.add(criteriaBuilder.ge(root.get(name), _double("ge." + name)));
        }
        if (nn("lt." + name)) {
            predicates.add(criteriaBuilder.lt(root.get(name), _double("lt." + name)));
        }
        if (nn("le." + name)) {
            predicates.add(criteriaBuilder.le(root.get(name), _double("le." + name)));
        }
    }

    public void buildLocalDateFieldFilters(String name, CriteriaBuilder criteriaBuilder, Root<T> root, List<Predicate> predicates){

        if (nn("from." + name)) {
            predicates.add(criteriaBuilder.greaterThan(root.get(name), _localDate("from." + name)));
        }
        if (nn("to." + name)) {
            predicates.add(criteriaBuilder.lessThan(root.get(name), _localDate("to." + name)));
        }
        if (nn("eq." + name)) {
            predicates.add(criteriaBuilder.equal(root.get(name), _localDate("eq." + name)));
        }
    }
}
