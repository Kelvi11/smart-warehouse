package com.kelvin.smartwarehouse.api;

import com.kelvin.api.service.BaseApi;
import com.kelvin.smartwarehouse.exception.InvalidParameterException;
import com.kelvin.smartwarehouse.model.OrderItem;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static com.kelvin.smartwarehouse.management.AppConstants.ORDER_ITEMS_URL;

@RestController
@RequestMapping(ORDER_ITEMS_URL)
public class OrderItemApi extends BaseApi<OrderItem> {

    public OrderItemApi() {
        super(OrderItem.class);
    }

    @Override
    protected String getDefaultOrderBy() {
        return "quantity desc";
    }

    protected List<Predicate> getFilters(CriteriaBuilder criteriaBuilder, Root<OrderItem> root){
        List<Predicate> predicates = new ArrayList<>();

        if (nn("obj.itemUuid")) {
            Path<String> orderUuid = root.get("itemUuid");
            predicates.add(criteriaBuilder.equal(orderUuid, get("obj.itemUuid")));
        }
        if (nn("obj.orderUuid")) {
            Path<String> orderUuid = root.get("orderUuid");
            predicates.add(criteriaBuilder.equal(orderUuid, get("obj.orderUuid")));
        }
        if (nn("eq.quantity")) {
            Path<Integer> quantity = root.get("quantity");
            predicates.add(criteriaBuilder.equal(quantity, _integer("eq.quantity")));
        }
        if (nn("gt.quantity")) {
            Path<Integer> quantity = root.get("quantity");
            predicates.add(criteriaBuilder.gt(quantity, _integer("gt.quantity")));
        }
        if (nn("ge.quantity")) {
            Path<Integer> quantity = root.get("quantity");
            predicates.add(criteriaBuilder.ge(quantity, _integer("ge.quantity")));
        }
        if (nn("lt.quantity")) {
            Path<Integer> quantity = root.get("quantity");
            predicates.add(criteriaBuilder.lt(quantity, _integer("lt.quantity")));
        }
        if (nn("le.quantity")) {
            Path<Integer> quantity = root.get("quantity");
            predicates.add(criteriaBuilder.le(quantity, _integer("le.quantity")));
        }

        return predicates;
    }

    @Override
    protected void prePersist(OrderItem orderItem) throws Exception {
        if (orderItem.getOrderUuid() == null || orderItem.getOrderUuid().isBlank()){
            throw new InvalidParameterException("Order item order uuid is required!");
        }
        if (orderItem.getItemUuid() == null || orderItem.getItemUuid().isBlank()){
            throw new InvalidParameterException("Order item uuid of item is required!");
        }
        if (orderItem.getQuantity() < 1){
            throw new InvalidParameterException("Order item quantity should be a positive number!");
        }
    }
}
