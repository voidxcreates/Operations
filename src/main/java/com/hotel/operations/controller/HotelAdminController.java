package com.hotel.operations.controller;

import com.hotel.operations.domain.AdminSummaryView;
import com.hotel.operations.domain.Hotel;
import com.hotel.operations.domain.Plan;
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
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class HotelAdminController {

    @Autowired
    private HotelAdminService adminService;

    @GetMapping("/summary")
    public ResponseEntity<AdminSummaryView> getSummary() {
        return ResponseEntity.ok(adminService.getDashboardSummary());
    }
    @GetMapping("/hotels")
    public ResponseEntity<Map<String, Object>> getHotels(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String plan,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Page<Hotel> filtered = adminService.getFilteredHotels(search, status, plan, page, size);
    
        List<HotelResponse> dtoList = filtered.getContent().stream()
            .map(adminService::mapToHotelResponse)
            .toList();
    
        Map<String, Object> result = new HashMap<>();
        result.put("items", dtoList);
        result.put("total", filtered.getTotalElements());
    
        return ResponseEntity.ok(result);
    }

    @PostMapping("/hotels")
    public ResponseEntity<Hotel> addHotel(@Valid @RequestBody CreateHotelRequest req) {
        return new ResponseEntity<>(adminService.createNewHotel(req), HttpStatus.CREATED);

    }


    @GetMapping("/plans")
    public ResponseEntity<List<Plan>> getPlans() {
        return ResponseEntity.ok(adminService.getAllActivePlans());
    }

    @GetMapping("/hotels/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long id) {
        Hotel hotel = adminService.getHotelById(id);
    
        HotelResponse responseDto = adminService.mapToHotelResponse(hotel);
    
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/hotels/{id}/status")
    public ResponseEntity<Void> toggleHotelStatus(
        @PathVariable Long id,
        @Valid @RequestBody UpdateStatusRequest req) {
        
        adminService.updateHotelStatus(id, req);
    
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/hotels/{id}/renew")
    public ResponseEntity<Void> renewHotelPlan(
            @PathVariable Long id
    ) {
        adminService.renewHotelPlan(id);

        return ResponseEntity.noContent().build();
    }
}