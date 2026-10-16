package com.hotel.operations.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.OffsetDateTime;

@Entity
@Table(name = "hotel_change_notifications")
@Getter @Setter
public class HotelChangeNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "changed_by", nullable = false)
    private Long changedBy;

    @Column(nullable = false)
    private String changes; // Maps JSON string configuration array

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "acknowledged_by")
    private Long acknowledgedBy;

    @Column(name = "acknowledged_at")
    private OffsetDateTime acknowledgedAt;
}
