package com.kelvin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;

public abstract class BaseApi<T> {

    protected final Class<T> entityClass;

    @Autowired
    EntityManager entityManager;

    protected BaseApi(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @GetMapping
    public ResponseEntity<T> getList(){
        return ResponseEntity.ok().build();
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
