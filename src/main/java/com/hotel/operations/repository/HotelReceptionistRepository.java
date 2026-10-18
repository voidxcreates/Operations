
package com.hotel.operations.repository;

import com.hotel.operations.domain.HotelReceptionist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HotelReceptionistRepository
        extends JpaRepository<HotelReceptionist, Long> {

    List<HotelReceptionist> findByHotel_HotelIdOrderByReceptionistIdAsc(
            Long hotelId
    );

    void deleteByHotel_HotelId(Long hotelId);

    @Modifying
    @Query("DELETE FROM HotelReceptionist r WHERE r.hotel.hotelId = :hotelId")
    void deleteByHotelId(@Param("hotelId") Long hotelId);
}
