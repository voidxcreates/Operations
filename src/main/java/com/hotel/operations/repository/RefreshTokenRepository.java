package com.hotel.operations.repository;
import com.hotel.operations.domain.*;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
