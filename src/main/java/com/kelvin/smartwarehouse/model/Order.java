package com.kelvin.smartwarehouse.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Order {

    private String uuid;

    private LocalDate submittedDate;
    private LocalDate deadlineDate;

    private OrderStatus status;
}
