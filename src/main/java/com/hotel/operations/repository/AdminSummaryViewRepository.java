package com.hotel.operations.repository;
import com.hotel.operations.domain.*;
import org.springframework.data.jpa.repository.*;

public interface AdminSummaryViewRepository extends JpaRepository<AdminSummaryView, Long> {
    @Query(value = "SELECT * FROM admin_summary_v FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    AdminSummaryView getSummary();
}