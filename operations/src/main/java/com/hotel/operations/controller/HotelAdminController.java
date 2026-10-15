package com.hotel.operations.controller;

import com.hotel.operations.domain.AdminSummaryView;
import com.hotel.operations.domain.Hotel;
import com.hotel.operations.dto.HotelRequests.CreateHotelRequest;
import com.hotel.operations.dto.HotelRequests.UpdateStatusRequest;
import com.hotel.operations.dto.HotelResponse;
import com.hotel.operations.service.HotelAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class HotelAdminController {

    @Autowired
    private HotelAdminService adminService;

    @GetMapping("/summary")
    public ResponseEntity<AdminSummaryView> getSummary() {
        return ResponseEntity.ok(adminService.getDashboardSummary());
    }
// Update the getHotels mapping method inside HotelAdminController.java
@GetMapping("/hotels")
public ResponseEntity<Map<String, Object>> getHotels(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String plan,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

    Page<Hotel> filtered = adminService.getFilteredHotels(search, status, plan, page, size);
    
    // Convert the database models into formatted DTOs matching the frontend spec
    java.util.List<HotelResponse> dtoList = filtered.getContent().stream()
            .map(adminService::mapToHotelResponse)
            .toList();
    
    Map<String, Object> result = new HashMap<>();
    result.put("items", dtoList); // Feeds the customized layout stream directly to React
    result.put("total", filtered.getTotalElements());
    
    return ResponseEntity.ok(result);
}

    @PostMapping("/hotels")
    public ResponseEntity<Hotel> addHotel(@Valid @RequestBody CreateHotelRequest req) {
        return new ResponseEntity<>(adminService.createNewHotel(req), HttpStatus.CREATED);

    }

    // Add these endpoint mappings inside your HotelAdminController class

// GET /api/admin/plans
@GetMapping("/plans")
public ResponseEntity<java.util.List<com.hotel.operations.domain.Plan>> getPlans() {
    return ResponseEntity.ok(adminService.getAllActivePlans());
}

// GET /api/admin/hotels/{id}
@GetMapping("/hotels/{id}")
public ResponseEntity<com.hotel.operations.dto.HotelResponse> getHotelById(@PathVariable Long id) {
    com.hotel.operations.domain.Hotel hotel = adminService.getHotelById(id);
    
    // 2. Convert it into the exact nested JSON structure the UI expects
    HotelResponse responseDto = adminService.mapToHotelResponse(hotel);
    
    // 3. Return the clean DTO with HTTP 200 OK
    return ResponseEntity.ok(responseDto);
}

@PatchMapping("/hotels/{id}/status")
public ResponseEntity<Void> toggleHotelStatus(
        @PathVariable Long id,
        @jakarta.validation.Valid @RequestBody UpdateStatusRequest req) {
        
    adminService.updateHotelStatus(id, req);
    
    // Returns HTTP 204 No Content matching your exact UI blueprint expectation
    return ResponseEntity.noContent().build();
}


}
