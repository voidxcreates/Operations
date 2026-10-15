package com.hotel.operations.repository;
import com.hotel.operations.domain.*;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
}