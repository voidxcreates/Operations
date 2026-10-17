package com.hotel.operations.service;

import com.hotel.operations.domain.AppUser;
import com.hotel.operations.domain.RefreshToken;
import com.hotel.operations.dto.AuthRequests.LoginRequest;
import com.hotel.operations.dto.AuthResponses.LoginResponse;
import com.hotel.operations.dto.AuthResponses.UserProfile;
import com.hotel.operations.repository.AppUserRepository;
import com.hotel.operations.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.authentication.BadCredentialsException;
import java.util.Optional;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.util.UUID;
import java.util.HexFormat;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    public record RefreshResult(
            LoginResponse loginResponse,
            String refreshToken
    ) {}

    @Autowired private AppUserRepository userRepository;
    @Autowired private RefreshTokenRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final JwtEncoder jwtEncoder;

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
        String accessToken = generateAccessToken(user);

        UserProfile profile = new UserProfile(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getUserRole()
        );

        return new LoginResponse(accessToken, profile);
    }

    public String generateAndSaveRefreshToken(Long userId) {
        String rawToken = UUID.randomUUID().toString();
        
        RefreshToken rt = new RefreshToken();
        rt.setUserId(userId);
        rt.setTokenHash(hashRefreshToken(rawToken));
        rt.setExpiresAt(OffsetDateTime.now().plusDays(7));
        tokenRepository.save(rt);
        
        return rawToken;
    }

    public UserProfile getUserProfileById(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User session expired"));
        return new UserProfile(user.getUserId(), user.getFullName(), user.getEmail(), user.getUserRole());
    }

    public void revokeRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        String tokenHash = hashRefreshToken(rawToken);

        tokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    if (token.getRevokedAt() == null) {
                        token.setRevokedAt(OffsetDateTime.now());
                        tokenRepository.save(token);
                    }
                });
    }

    private String generateAccessToken(AppUser user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("hotel-operations")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(15 * 60))
                .subject(user.getEmail())
                .claim("userId", user.getUserId())
                .claim("role", user.getUserRole())
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();
    }

    private String hashRefreshToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    public RefreshResult rotateRefreshToken(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new BadCredentialsException("Refresh token is missing");
        }

        String tokenHash = hashRefreshToken(rawRefreshToken);

        RefreshToken oldToken = tokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid refresh token"));

        if (oldToken.getRevokedAt() != null) {
            throw new BadCredentialsException("Refresh token has been revoked");
        }

        if (!oldToken.getExpiresAt().isAfter(OffsetDateTime.now())) {
            throw new BadCredentialsException("Refresh token has expired");
        }

        AppUser user = userRepository.findById(oldToken.getUserId())
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid refresh token"));

        if (user.getIsActive() == 0) {
            throw new BadCredentialsException("User account is inactive");
        }

        // Revoke the existing token.
        oldToken.setRevokedAt(OffsetDateTime.now());
        tokenRepository.save(oldToken);

        // Generate and persist its replacement in the same transaction.
        String newRefreshToken =
                generateAndSaveRefreshToken(user.getUserId());

        // Generate a new access JWT.
        String accessToken = generateAccessToken(user);

        UserProfile profile = new UserProfile(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getUserRole()
        );

        LoginResponse loginResponse =
                new LoginResponse(accessToken, profile);

        return new RefreshResult(loginResponse, newRefreshToken);
    }
}
