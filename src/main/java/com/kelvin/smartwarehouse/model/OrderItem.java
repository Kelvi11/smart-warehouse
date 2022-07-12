package com.kelvin.smartwarehouse.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {

    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "uuid", unique = true)
    @Id
    private String uuid;

    @Column(name = "item_uuid")
    private String itemUuid;

    @Column(name = "order_uuid")
    private String orderUuid;

    private int quantity;
}
