package com.kelvin.smartwarehouse.api;

import com.kelvin.api.service.BaseApi;
import com.kelvin.smartwarehouse.exception.InvalidParameterException;
import com.kelvin.smartwarehouse.model.Order;
import com.kelvin.smartwarehouse.model.OrderItem;
import org.hibernate.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static com.kelvin.smartwarehouse.management.AppConstants.ORDERS_URL;
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

    @Override
    public void applyFilters() throws Exception {
        if (nn("like.created_by")) {
            getEntityManager().unwrap(Session.class)
                    .enableFilter("like.created_by")
                    .setParameter("created_by", likeParamToLowerCase("like.created_by"));
        }
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
