package com.hotel.operations.service;

import com.hotel.operations.domain.AppUser;
import com.hotel.operations.domain.RefreshToken;
import com.hotel.operations.dto.AuthRequests.LoginRequest;
import com.hotel.operations.dto.AuthResponses.LoginResponse;
import com.hotel.operations.dto.AuthResponses.UserProfile;
import com.hotel.operations.repository.AppUserRepository;
import com.hotel.operations.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    @Autowired private AppUserRepository userRepository;
    @Autowired private RefreshTokenRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public LoginResponse authenticate(LoginRequest req) {
        // 1. Fetch user from Oracle, forcing lowercase tracking for email
        AppUser user = userRepository.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // 2. Cryptographic verify using BCrypt
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (user.getIsActive() == 0) {
            throw new RuntimeException("This account has been deactivated");
        }

        // 3. Generate tokens matching your frontend blueprint requirements
        String mockAccessToken = "mock-jwt-" + UUID.randomUUID().toString();
        
        UserProfile profile = new UserProfile(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getUserRole()
        );

        return new LoginResponse(mockAccessToken, profile);
    }

    public String generateAndSaveRefreshToken(Long userId) {
        String rawToken = UUID.randomUUID().toString();
        
        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setTokenHash(rawToken); // In production, you would hash this string
        rt.setExpiresAt(OffsetDateTime.now().plusDays(7));
        tokenRepository.save(rt);
        
        return rawToken;
    }

    public UserProfile getUserProfileById(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User session expired"));
        return new UserProfile(user.getUserId(), user.getFullName(), user.getEmail(), user.getUserRole());
    }
}
