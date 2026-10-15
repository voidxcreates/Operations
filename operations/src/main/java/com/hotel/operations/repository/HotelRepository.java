package com.hotel.operations.repository;

import com.hotel.operations.domain.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query("SELECT h FROM Hotel h JOIN FETCH h.plan p WHERE " +
           "(:status IS NULL OR h.status = :status) AND " +
           "(:planCode IS NULL OR p.code = :planCode) AND " +
           "(:search IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(h.city) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(h.ownerName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR h.ownerPhone LIKE CONCAT('%', :search, '%'))")
    Page<Hotel> findByAdminFilters(@Param("search") String search,
                                  @Param("status") String status,
                                  @Param("planCode") String planCode,
                                  Pageable pageable);
}
