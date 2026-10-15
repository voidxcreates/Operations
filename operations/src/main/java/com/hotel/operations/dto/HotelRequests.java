package com.hotel.operations.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public class HotelRequests {
    public record NestedPerson(String name, String phone, String email) {}
    public record NestedStaff(String name, String phone) {}
    
    public record CreateHotelRequest(
        @NotBlank String name, String addressLine, @NotBlank String city, String state,
        @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be exactly 6 digits") String pincode,
        @Min(1) @Max(500) Integer roomCount, @NotBlank String planCode, @NotBlank String joinedAt,
        @Valid NestedPerson owner, @Valid NestedStaff receptionist
    ) {}

    public record UpdateHotelRequest(
        @NotBlank String name, String addressLine, @NotBlank String city, String state,
        @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be exactly 6 digits") String pincode,
        @Min(1) @Max(500) Integer roomCount, @Valid NestedPerson owner, @Valid NestedStaff receptionist
    ) {}

    public record UpdatePlanRequest(@NotBlank String planCode) {}
    public record AcknowledgeRequest(List<Long> ids) {}

     public record UpdateStatusRequest(
        @NotBlank(message = "Status condition target is mandatory")
        @Pattern(regexp = "^(ACTIVE|SUSPENDED)$", message = "Status must be either ACTIVE or SUSPENDED")
        String status,
        
        String reason
    ) {}
}