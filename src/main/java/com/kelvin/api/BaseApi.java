package com.kelvin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public abstract class BaseApi<T> {

    protected final Class<T> entityClass;

    @Autowired
    EntityManager entityManager;

    protected BaseApi(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }

    @GetMapping
    public ResponseEntity getList(){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(getEntityClass());
        Root<T> rootEntry = cq.from(getEntityClass());
        CriteriaQuery<T> all = cq.select(rootEntry);
        TypedQuery<T> allQuery = entityManager.createQuery(all);
        List<T> list = allQuery.getResultList();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<T> persist(@RequestBody T object){
        entityManager.persist(object);
        return ResponseEntity.ok(object);
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> fetch(@PathVariable String id){
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<T> update(@PathVariable String id, T object){
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<T> delete(@PathVariable String id){
        return ResponseEntity.ok().build();
    }
}
