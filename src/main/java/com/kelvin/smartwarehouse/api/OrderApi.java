package com.kelvin.smartwarehouse.api;

import com.kelvin.api.service.BaseApi;
import com.kelvin.smartwarehouse.exception.InvalidParameterException;
import com.kelvin.smartwarehouse.model.Order;
import com.kelvin.smartwarehouse.model.enums.OrderStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.kelvin.smartwarehouse.management.AppConstants.ORDERS_URL;

@RestController
@RequestMapping(ORDERS_URL)
public class OrderApi extends BaseApi<Order> {

    public OrderApi() {
        super(Order.class);
    }

    @Override
    protected String getDefaultOrderBy() {
        return "deadlineDate desc";
    }

    @Override
    protected List<Predicate> getFilters(CriteriaBuilder criteriaBuilder, Root<Order> root){
        List<Predicate> predicates = new ArrayList<>();

        if (nn("obj.status")) {
            Path<OrderStatus> orderUuid = root.get("status");
            predicates.add(criteriaBuilder.equal(orderUuid, OrderStatus.valueOf(get("obj.status"))));
        }

        buildLocalDateFieldFilters("submittedDate", criteriaBuilder, root, predicates);

        buildLocalDateFieldFilters("deadlineDate", criteriaBuilder, root, predicates);

        return predicates;
    }

    @Override
    protected void prePersist(Order order) throws Exception {
        if (order.getDeadlineDate() == null){
            throw new InvalidParameterException("Order deadline date is required!");
        }
        if (order.getDeadlineDate().isBefore(LocalDate.now())){
            throw new InvalidParameterException("Order deadline date should be in the future!");
        }
    }
}
