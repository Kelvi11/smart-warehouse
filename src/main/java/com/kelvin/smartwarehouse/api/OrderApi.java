package com.kelvin.smartwarehouse.api;

import com.kelvin.api.service.BaseApi;
import com.kelvin.smartwarehouse.exception.InvalidParameterException;
import com.kelvin.smartwarehouse.model.Order;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static com.kelvin.smartwarehouse.management.AppConstants.ORDERS_URL;

@RestController
@RequestMapping(ORDERS_URL)
public class OrderApi extends BaseApi<Order> {

    public OrderApi() {
        super(Order.class);
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
