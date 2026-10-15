package com.hotel.operations.service;

import com.hotel.operations.domain.*;
import com.hotel.operations.dto.HotelRequests.*;
import com.hotel.operations.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import com.hotel.operations.dto.HotelResponse;
import com.hotel.operations.dto.HotelResponse.*;

@Service
@Transactional
public class HotelAdminService {

    @Autowired private HotelRepository hotelRepository;
    @Autowired private PlanRepository planRepository;
    @Autowired private AdminSummaryViewRepository summaryRepository;

    public AdminSummaryView getDashboardSummary() {
        return summaryRepository.getSummary();
    }

    public Page<Hotel> getFilteredHotels(String search, String status, String plan, int page, int size) {
        return hotelRepository.findByAdminFilters(search, status, plan, PageRequest.of(page, size));
    }

    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id).orElseThrow(() -> new RuntimeException("Hotel profile not found"));
    }

    public Hotel createNewHotel(CreateHotelRequest req) {
        Plan plan = planRepository.findByCode(req.planCode())
                .orElseThrow(() -> new RuntimeException("Invalid plan selection code code"));

        Hotel hotel = new Hotel();
        hotel.setName(req.name());
        hotel.setAddressLine(req.addressLine());
        hotel.setCity(req.city());
        hotel.setState(req.state());
        hotel.setPincode(req.pincode());
        hotel.setRoomCount(req.roomCount());
        hotel.setPlan(plan);
        
        LocalDate parsedDate  = LocalDate.parse(req.joinedAt());
        OffsetDateTime joinedDateTime = parsedDate.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();

        hotel.setJoinedAt(joinedDateTime);
        hotel.setPlanEndsAt(hotel.getJoinedAt().plusMonths(plan.getDurationMonths()));

        if (req.owner() != null) {
            hotel.setOwnerName(req.owner().name());
            hotel.setOwnerPhone(req.owner().phone());
            hotel.setOwnerEmail(req.owner().email());
        }
        if (req.receptionist() != null) {
            hotel.setReceptionistName(req.receptionist().name());
            hotel.setReceptionistPhone(req.receptionist().phone());
        }

        return hotelRepository.save(hotel);
    }

    // Add this method to your HotelAdminService class
    public java.util.List<Plan> getAllActivePlans() {
        return planRepository.findByIsActive(1);
}
// Add these imports to the top of HotelAdminService.java


// Add this mapping method inside your HotelAdminService class
public HotelResponse mapToHotelResponse(Hotel hotel) {
    long pricePerRoom = hotel.getPlan() != null ? hotel.getPlan().getPricePerRoom() : 0L;
    
    // Calculates the metric the React UI tries to read: rooms * per-room rate
    long monthlyAmount = hotel.getRoomCount() * pricePerRoom;

    PlanSummary planSummary = hotel.getPlan() != null ? 
        new PlanSummary(hotel.getPlan().getCode(), hotel.getPlan().getName(), pricePerRoom) : null;

    // Use fallback strings ("-") if database entries are blank or null to match your UI presentation
    PersonSummary ownerSummary = new PersonSummary(
        hotel.getOwnerName() != null ? hotel.getOwnerName() : "-",
        hotel.getOwnerPhone() != null ? hotel.getOwnerPhone() : "-",
        hotel.getOwnerEmail() != null ? hotel.getOwnerEmail() : "-"
    );

    StaffSummary receptionistSummary = new StaffSummary(
        hotel.getReceptionistName() != null ? hotel.getReceptionistName() : "-",
        hotel.getReceptionistPhone() != null ? hotel.getReceptionistPhone() : "-"
    );
    return new HotelResponse(
        hotel.getHotelId(),
        hotel.getName(),
        hotel.getAddressLine(),
        hotel.getCity(),
        hotel.getState(),
        hotel.getPincode(),
        hotel.getRoomCount(),
        planSummary,
        monthlyAmount,
        hotel.getStatus(),
        hotel.getJoinedAt(),
        hotel.getPlanEndsAt(),
        ownerSummary,
        receptionistSummary
    );
}


@org.springframework.transaction.annotation.Transactional
public void updateHotelStatus(Long id, com.hotel.operations.dto.HotelRequests.UpdateStatusRequest req) {
    Hotel hotel = hotelRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Target hotel instance not found"));

    // Modify target fields reflecting structural request modifications
    hotel.setStatus(req.status());
    hotel.setSuspendedReason(req.status().equals("SUSPENDED") ? req.reason() : null);
    hotel.setUpdatedAt(java.time.OffsetDateTime.now());

    // The transaction automatically commits updates back to Oracle on execution finish
    hotelRepository.save(hotel);
}

}
