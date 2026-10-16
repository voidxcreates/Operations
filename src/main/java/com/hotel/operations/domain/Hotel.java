package com.hotel.operations.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "hotels")
@Getter @Setter
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hotel_id")
    private Long hotelId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "room_count", nullable = false)
    private Integer roomCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false, length = 120)
    private String status = "ACTIVE";

    @Column(name = "suspended_reason", length = 300)
    private String suspendedReason;

    @Column(name = "trial_ends_at")
    private OffsetDateTime trialEndsAt;

    @Column(name = "owner_name", length = 120)
    private String ownerName;

    @Column(name = "owner_phone", length = 10)
    private String ownerPhone;

    @Column(name = "owner_email", length = 200)
    private String ownerEmail;

    @Column(name = "address_line", length = 250)
    private String addressLine;

    @Column(length = 6)
    private String pincode;

    @Column(name = "receptionist_name", length = 120)
    private String receptionistName;

    @Column(name = "receptionist_phone", length = 10)
    private String receptionistPhone;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @Column(name = "plan_ends_at", nullable = false)
    private OffsetDateTime planEndsAt;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt = OffsetDateTime.now(); // Add this line!

}
