package com.hotel.operations.repository;
import com.hotel.operations.domain.*;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByCode(String code);
    List<Plan> findByIsActive(Integer isActive);
}


