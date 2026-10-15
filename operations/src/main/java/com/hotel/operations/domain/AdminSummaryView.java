package com.hotel.operations.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.Immutable;

@Entity
@Table(name = "admin_summary_v")
@Immutable
@Getter
public class AdminSummaryView {

    @Id
    @Column(name = "total_hotels")
    private Long totalHotels;

    @Column(name = "active_hotels")
    private Long activeHotels;

    @Column(name = "trial_hotels")
    private Long trialHotels;

    @Column(name = "ending_soon")
    private Long endingSoon;

    @Column(name = "monthly_revenue")
    private Long monthlyRevenue;
}
