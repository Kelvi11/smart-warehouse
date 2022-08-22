package com.kelvin.smartwarehouse.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "truck")
public class Truck {

    @Id
    @Column(name = "chassis_number")
    private String chassisNumber;

    @Column(name = "license_plate", unique = true)
    private String licensePlate;

    @Column(name = "container_volume")
    private double containerVolume;

}
