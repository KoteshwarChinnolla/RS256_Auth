package com.auth.jwtsecurity.controller;

import com.auth.jwtsecurity.dto.LoginRequest;
import com.auth.jwtsecurity.dto.RegisterRequest;
import com.auth.jwtsecurity.dto.TokenPair;
import com.auth.jwtsecurity.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        TokenPair tokenPair = authService.login(loginRequest);

        //  token cookie part in http
        Cookie refreshTokenCookie = new Cookie("refreshToken", tokenPair.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshTokenCookie);
        // Return only the access token in body
        return ResponseEntity.ok(Map.of(
                "accessToken", tokenPair.getAccessToken()
        ));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing or empty refresh token");
        }

        TokenPair tokenPair = authService.refreshTokenFromCookie(refreshToken);

        return ResponseEntity.ok(Map.of(
                "accessToken", tokenPair.getAccessToken()
        ));
    }
}
