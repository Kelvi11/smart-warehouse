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
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kelvin.smartwarehouse.management.AppConstants.ORDER_BY_ASC;
import static com.kelvin.smartwarehouse.management.AppConstants.ORDER_BY_DESC;

public abstract class BaseApi<T> extends HttpBase{

    protected final Class<T> entityClass;

    @Autowired
    EntityManager entityManager;

    protected BaseApi(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    @GetMapping
    @Transactional
    public ResponseEntity getList(
            @RequestParam(value = "startRow", required = false, defaultValue = "0") Integer startRow,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "orderBy", required = false) String orderBy
    ) throws Exception {

        applyFilters();

        long listSize = count();
        List<T> list;
        if (listSize == 0) {
            list = new ArrayList<>();
        } else {
            int currentPage = 0;
            if (pageSize != 0) {
                currentPage = startRow / pageSize;
            } else {
                pageSize = Long.valueOf(listSize).intValue();
            }
            TypedQuery<T> search = getSearch(orderBy);
            list = search.setFirstResult(startRow)
                    .setMaxResults(pageSize)
                    .getResultList();
        }

        return ResponseEntity.ok()
                .header("startRow", String.valueOf(startRow))
                .header("pageSize", String.valueOf(pageSize))
                .header("listSize", String.valueOf(listSize))
                .body(list);
    }

    private long count(){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countCriteriaQuery = criteriaBuilder.createQuery(Long.class);
        countCriteriaQuery.select(criteriaBuilder.count(countCriteriaQuery.from(getEntityClass())));

        return entityManager.createQuery(countCriteriaQuery).getSingleResult();
    }

    private TypedQuery<T> getSearch(String orderBy){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(getEntityClass());
        Root<T> root = criteriaQuery.from(getEntityClass());
        criteriaQuery.select(root);

        List<Order> orderList = sort(orderBy, criteriaBuilder, root);
        criteriaQuery.orderBy(orderList);

        TypedQuery<T> search = entityManager.createQuery(criteriaQuery);
        return search;
    }

    private List<Order> sort(String orderBy, CriteriaBuilder criteriaBuilder, Root<T> routeRoot) {
        List<Order> orderList = new ArrayList<>();

        List<String> orderByExpresions;
        if (orderBy != null){
            orderByExpresions = fromValueToList(orderBy);
        }
        else {
            orderByExpresions = fromValueToList(getDefaultOrderBy());
        }

        for (String orderByExpresion : orderByExpresions){
            if (orderByExpresion.contains(ORDER_BY_ASC)){
                String property = orderByExpresion.replace(ORDER_BY_ASC, "").trim();
                orderList.add(criteriaBuilder.asc(routeRoot.get(property)));
            }
            else if(orderByExpresion.contains(ORDER_BY_DESC)){
                String property = orderByExpresion.replace(ORDER_BY_DESC, "").trim();
                orderList.add(criteriaBuilder.desc(routeRoot.get(property)));
            }
        }
        return orderList;
    }

    public void applyFilters() throws Exception {

    }
    protected abstract String getDefaultOrderBy();

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
    @Transactional
    public ResponseEntity<T> fetch(@PathVariable String id){

        if (id == null || id.isBlank()){
            throw new IdMissingException();
        }

        T t = getTByIdOrThrowException(id);

        return ResponseEntity.ok(t);
    }

    private T getTByIdOrThrowException(String id) {
        T t = entityManager.find(getEntityClass(), id);

        if (t == null){

            String classSimpleName = getEntityClass().getSimpleName();
            String entityNameAsWords = StringUtil.fromCamelCaseToSeparatedWordsWhenFirstWordStartsWithCapitalLetter(classSimpleName);

            throw new EntityWithIdNotFoundException(String.format("%s with id [%s] doesn't exist in database!", entityNameAsWords, id));
        }
        return t;
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<T> update(@PathVariable String id, @RequestBody T object){
        entityManager.merge(object);
        return ResponseEntity.ok(object);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<T> delete(@PathVariable String id){

        T t = getTByIdOrThrowException(id);
        toDelete(t);
        return ResponseEntity.noContent().build();
    }

    protected void toDelete(T t) {
        entityManager.remove(t);
    }
}
