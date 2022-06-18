package com.kelvin.smartwarehouse.api;

import com.kelvin.api.BaseApi;
import com.kelvin.smartwarehouse.model.Order;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.kelvin.smartwarehouse.management.AppConstants.ORDERS_URL;

@RestController
@RequestMapping(ORDERS_URL)
public class OrderApi extends BaseApi<Order> {

    public OrderApi() {
        super(Order.class);
    }
}
