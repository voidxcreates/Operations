package com.hotel.operations.service;

import com.hotel.operations.domain.*;
import com.hotel.operations.dto.HotelRequests.*;
import com.hotel.operations.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;

import java.time.*;
import java.util.List;
import com.hotel.operations.dto.HotelResponse;
import com.hotel.operations.dto.HotelResponse.*;

@Service
@Transactional
public class HotelAdminService {

    @Autowired private HotelRepository hotelRepository;
    @Autowired private PlanRepository planRepository;
    @Autowired private AdminSummaryViewRepository summaryRepository;

    @Autowired
    private HotelReceptionistRepository receptionistRepository;

    public AdminSummaryView getDashboardSummary() {
        return summaryRepository.getSummary();
    }

    public Page<Hotel> getFilteredHotels(String search, String status, String plan, int page, int size) {
        return hotelRepository.findByAdminFilters(search, status, plan, PageRequest.of(page, size));
    }

    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id).orElseThrow(() -> new RuntimeException("Hotel profile not found"));
    }

    public Hotel createNewHotel(CreateHotelRequest req, Long createdBy){
        Plan plan = planRepository.findByCode(req.planCode())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid plan selection code"));

        if (plan.getIsActive() == null || plan.getIsActive() != 1) {
            throw new IllegalArgumentException("Selected plan is not active");
        }

        if (plan.getMaxRooms() != null
                && req.roomCount() > plan.getMaxRooms()) {
            throw new IllegalArgumentException(
                    "Room count exceeds the maximum allowed by the selected plan");
        }


        Hotel hotel = new Hotel();
        hotel.setCreatedBy(createdBy);
        hotel.setName(req.name());
        hotel.setAddressLine(req.addressLine());
        hotel.setCity(req.city());
        hotel.setState(req.state());
        hotel.setPincode(req.pincode());
        hotel.setRoomCount(req.roomCount());
        hotel.setPlan(plan);
        hotel.setStatus("ACTIVE");
        
        LocalDate parsedDate  = LocalDate.parse(req.joinedAt());
        OffsetDateTime joinedDateTime = parsedDate.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();

        hotel.setJoinedAt(joinedDateTime);
        hotel.setPlanEndsAt(hotel.getJoinedAt().plusMonths(plan.getDurationMonths()));

        if (req.owner() != null) {
            hotel.setOwnerName(req.owner().name());
            hotel.setOwnerPhone(req.owner().phone());
            hotel.setOwnerEmail(req.owner().email());
        }

        Hotel savedHotel = hotelRepository.save(hotel);
        replaceReceptionists(savedHotel, req.receptionists());
        return savedHotel;
    }

    // Add this method to your HotelAdminService class
    public List<Plan> getAllActivePlans() {
        return planRepository.findByIsActive(1);
}

    public HotelResponse mapToHotelResponse(Hotel hotel) {
        long pricePerRoom = hotel.getPlan() != null ? hotel.getPlan().getPricePerRoom() : 0L;
    
        long monthlyAmount = hotel.getRoomCount() * pricePerRoom;

        PlanSummary planSummary = hotel.getPlan() != null ?
            new PlanSummary(hotel.getPlan().getCode(), hotel.getPlan().getName(), pricePerRoom) : null;

        PersonSummary ownerSummary = new PersonSummary(
            hotel.getOwnerName() != null ? hotel.getOwnerName() : "-",
            hotel.getOwnerPhone() != null ? hotel.getOwnerPhone() : "-",
            hotel.getOwnerEmail() != null ? hotel.getOwnerEmail() : "-"
        );

        List<ReceptionistSummary> receptionists =
                receptionistRepository
                        .findByHotel_HotelIdOrderByReceptionistIdAsc(
                                hotel.getHotelId())
                        .stream()
                        .map(receptionist -> new ReceptionistSummary(
                                receptionist.getReceptionistId(),
                                receptionist.getFullName(),
                                receptionist.getPhone()))
                        .toList();
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
            receptionists
        );
    }


    @Transactional
    public void updateHotelStatus(Long id, UpdateStatusRequest req) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Target hotel instance not found"));

        hotel.setStatus(req.status());
        hotel.setSuspendedReason(req.status().equals("SUSPENDED") ? req.reason() : null);
        hotel.setUpdatedAt(java.time.OffsetDateTime.now());

        hotelRepository.save(hotel);
    }

    public void renewHotelPlan(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Target hotel instance not found"));

        OffsetDateTime currTime = OffsetDateTime.now();
        if(hotel.getStatus().equals("SUSPENDED")){
            hotel.setStatus("ACTIVE");
            hotel.setPlanEndsAt(currTime.plusMonths(hotel.getPlan().getDurationMonths()));
        }
        else{
            hotel.setPlanEndsAt(hotel.getPlanEndsAt().plusMonths(hotel.getPlan().getDurationMonths()));
        }

        hotel.setUpdatedAt(currTime);

        hotelRepository.save(hotel);
    }


    public Hotel updateHotel(Long id, UpdateHotelRequest req) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel profile not found"));

        Plan currentPlan = hotel.getPlan();

        if (currentPlan.getMaxRooms() != null
                && req.roomCount() > currentPlan.getMaxRooms()) {
            throw new IllegalArgumentException(
                    "Room count exceeds the maximum allowed by the hotel's current plan");
        }

        hotel.setName(req.name());
        hotel.setAddressLine(req.addressLine());
        hotel.setCity(req.city());
        hotel.setState(req.state());
        hotel.setPincode(req.pincode());
        hotel.setRoomCount(req.roomCount());

        if (req.owner() != null) {
            hotel.setOwnerName(req.owner().name());
            hotel.setOwnerPhone(req.owner().phone());
            hotel.setOwnerEmail(req.owner().email());
        } else {
            hotel.setOwnerName(null);
            hotel.setOwnerPhone(null);
            hotel.setOwnerEmail(null);
        }


        hotel.setUpdatedAt(OffsetDateTime.now());

        Hotel savedHotel = hotelRepository.save(hotel);
        replaceReceptionists(savedHotel, req.receptionists());

        return savedHotel;
    }


    public void updateHotelPlan(Long id, UpdatePlanRequest req) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Hotel profile not found"));

        Plan newPlan = planRepository.findByCode(req.planCode())
                .orElseThrow(() ->
                        new RuntimeException("Invalid plan selection code"));

        if (newPlan.getIsActive() == null || newPlan.getIsActive() != 1) {
            throw new RuntimeException("Selected plan is inactive");
        }

        // Reject the change if the hotel exceeds the new plan's room limit.
        // A null maxRooms means no room limit is configured.
        if (newPlan.getMaxRooms() != null
                && hotel.getRoomCount() > newPlan.getMaxRooms()) {
            throw new RuntimeException(
                    "Hotel room count exceeds the maximum rooms allowed by the selected plan");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        hotel.setPlan(newPlan);
        hotel.setPlanEndsAt(now.plusMonths(newPlan.getDurationMonths()));
        hotel.setUpdatedAt(now);

        hotelRepository.save(hotel);
    }


    private void replaceReceptionists(
            Hotel hotel,
            List<NestedStaff> receptionists) {

        // Treat a missing list as an empty list.
        List<NestedStaff> requested =
                receptionists != null ? receptionists : List.of();

        if (requested.size() > 5) {
            throw new IllegalArgumentException(
                    "A hotel cannot have more than 5 receptionists");
        }

        receptionistRepository.deleteByHotelId(hotel.getHotelId());

        // Save the replacement list.
        List<HotelReceptionist> entities = new ArrayList<>();

        for (NestedStaff staff : requested) {
            HotelReceptionist receptionist = new HotelReceptionist();
            receptionist.setHotel(hotel);
            receptionist.setFullName(staff.name());
            receptionist.setPhone(staff.phone());
            entities.add(receptionist);
        }

        receptionistRepository.saveAll(entities);
    }


}
