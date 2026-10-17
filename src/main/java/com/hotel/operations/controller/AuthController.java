package com.hotel.operations.controller;

import com.hotel.operations.dto.AuthRequests.LoginRequest;
import com.hotel.operations.dto.AuthResponses.LoginResponse;
import com.hotel.operations.dto.AuthResponses.UserProfile;
import com.hotel.operations.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginReq, 
            HttpServletResponse response) {
        
        // Authenticate credentials
        LoginResponse loginResponse = authService.authenticate(loginReq);
        
        // Generate raw refresh token string
        String refreshToken = authService.generateAndSaveRefreshToken(loginResponse.user().id());
        
        // Build secure HttpOnly Cookie matching your React credentials rule
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true later in production when running on HTTPS
        cookie.setPath("/"); // Limits cookies to validation endpoints only
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 Days duration matching token entity
        
        response.addCookie(cookie);
        
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> getMe(
            @AuthenticationPrincipal Jwt jwt) {

        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        Object userIdClaim = jwt.getClaim("userId");

        if (!(userIdClaim instanceof Number)) {
            return ResponseEntity.status(401).build();
        }

        Long userId = ((Number) userIdClaim).longValue();

        UserProfile user = authService.getUserProfileById(userId);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        // Revoke the refresh token in the database
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.revokeRefreshToken(refreshToken);
        }

        // Expire the refresh-token cookie
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production when using HTTPS
        cookie.setPath("/");     // Must match the path used during login
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = "refreshToken", required = false)
            String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        AuthService.RefreshResult result =
                authService.rotateRefreshToken(refreshToken);

        Cookie cookie = new Cookie(
                "refreshToken",
                result.refreshToken()
        );
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set true in production over HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);

        return ResponseEntity.ok(result.loginResponse());
    }
}
