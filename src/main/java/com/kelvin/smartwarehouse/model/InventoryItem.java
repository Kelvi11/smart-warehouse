package com.kelvin.smartwarehouse.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "uuid", unique = true)
    @Id
    private String uuid;

    private String name;

    private int quantity;

    @Column(name = "unit_price")
    private double unitPrice;

    @Column(name = "package_volume")
    private double packageVolume;
}
