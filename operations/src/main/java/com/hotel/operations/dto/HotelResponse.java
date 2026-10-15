package com.hotel.operations.dto;

import java.time.OffsetDateTime;

public record HotelResponse(
    Long id,
    String name,
    String addressLine,
    String city,
    String state,
    String pincode,
    Integer roomCount,
    PlanSummary plan,
    Long monthlyAmount, // Exposes the direct calculation to fix the NaN bug
    String status,
    OffsetDateTime joinedAt,
    OffsetDateTime planEndsAt,
    PersonSummary owner,
    StaffSummary receptionist
) {
    public record PlanSummary(String code, String name, Long pricePerRoom) {}
    public record PersonSummary(String name, String phone, String email) {}
    public record StaffSummary(String name, String phone) {}
}
