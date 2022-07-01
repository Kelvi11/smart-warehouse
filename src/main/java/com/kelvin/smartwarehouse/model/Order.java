package com.kelvin.smartwarehouse.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "uuid", unique = true)
    @Id
    private String uuid;

    private LocalDate submittedDate;
    private LocalDate deadlineDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
