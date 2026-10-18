package com.hotel.operations.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

public class HotelRequests {

    public record NestedPerson(
            @NotBlank(message = "Owner name is required")
            @Size(max = 120, message = "Owner name cannot exceed 120 characters")
            String name,

            @Pattern(
                    regexp = "^[0-9]{10}$",
                    message = "Owner phone must be exactly 10 digits"
            )
            String phone,

            @Email(message = "Owner email must be valid")
            @Size(max = 200, message = "Owner email cannot exceed 200 characters")
            String email
    ) {}


    public record NestedStaff(
            @NotBlank(message = "Receptionist name is required")
            @Size(max = 120, message = "Receptionist name cannot exceed 120 characters")
            String name,

            @NotBlank(message = "Receptionist phone is required")
            @Pattern(
                    regexp = "^[0-9]{10}$",
                    message = "Receptionist phone must be exactly 10 digits"
            )
            String phone
    ) {}


    public record CreateHotelRequest(
        @NotBlank(message = "Hotel name is required")
        @Size(max = 150, message = "Hotel name cannot exceed 150 characters")
        String name, String addressLine, @NotBlank String city, String state,
        @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be exactly 6 digits") String pincode,
        @NotNull(message = "Room count is required")
        @Min(value = 1, message = "Room count must be at least 1")
        @Max(value = 500, message = "Room count cannot exceed 500")
        Integer roomCount, @NotBlank String planCode,
        @NotBlank(message = "Joining date is required")
        @Pattern(
                regexp = "^\\d{4}-\\d{2}-\\d{2}$",
                message = "Joining date must be in yyyy-MM-dd format"
        )
        String joinedAt,
        @Valid NestedPerson owner, @Valid
        @Size(max = 5, message = "A hotel cannot have more than 5 receptionists")
        List<@NotNull @Valid NestedStaff> receptionists
    ) {}

    public record UpdateHotelRequest(
        @NotBlank(message = "Hotel name is required")
        @Size(max = 150, message = "Hotel name cannot exceed 150 characters")
        String name, String addressLine, @NotBlank String city, String state,
        @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be exactly 6 digits") String pincode,
        @NotNull(message = "Room count is required")
        @Min(value = 1, message = "Room count must be at least 1")
        @Max(value = 500, message = "Room count cannot exceed 500")
        Integer roomCount, @Valid NestedPerson owner, @Valid
        @Size(max = 5, message = "A hotel cannot have more than 5 receptionists")
        List<@NotNull @Valid NestedStaff> receptionists
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