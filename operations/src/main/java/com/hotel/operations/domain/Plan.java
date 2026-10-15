package com.hotel.operations.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "plans")
@Getter @Setter
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    @Column(unique = true, nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(name = "max_rooms")
    private Integer maxRooms;

    @Column(name = "is_active", nullable = false)
    private Integer isActive = 1;

    @Column(name = "price_per_room", nullable = false)
    private Long pricePerRoom = 0L;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths = 1;
}
