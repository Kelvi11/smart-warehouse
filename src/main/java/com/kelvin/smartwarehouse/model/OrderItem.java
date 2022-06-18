package com.kelvin.smartwarehouse.model;

import lombok.Data;

@Data
public class OrderItem {

    private String uuid;
    private String item_uuid;
    private String order_uuid;

    private int quantity;
}
