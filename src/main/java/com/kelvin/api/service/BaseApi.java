package com.kelvin.api.service;

import com.kelvin.api.util.StringUtil;
import com.kelvin.smartwarehouse.exception.EntityWithIdNotFoundException;
import com.kelvin.smartwarehouse.exception.IdMissingException;
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
    public ResponseEntity<T> persist(@RequestBody T object) throws Exception {
        prePersist(object);
        entityManager.persist(object);
        return ResponseEntity.ok(object);
    }

    protected void prePersist(T object) throws Exception{
    }

    @GetMapping("/{id}")
    public ResponseEntity<T> fetch(@PathVariable String id){

        if (id == null || id.isBlank()){
            throw new IdMissingException();
        }

        T t = entityManager.find(getEntityClass(), id);

        if (t == null){

            String classSimpleName = getEntityClass().getSimpleName();
            String entityNameAsWords = StringUtil.fromCamelCaseToSeparatedWordsWhenFirstWordStartsWithCapitalLetter(classSimpleName);

            throw new EntityWithIdNotFoundException(String.format("%s with id [%s] doesn't exist in database!", entityNameAsWords, id));
        }

        return ResponseEntity.ok(t);
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
