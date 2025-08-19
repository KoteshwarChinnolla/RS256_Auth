package com.auth.jwtsecurity.service;

import com.auth.jwtsecurity.dto.LoginRequest;
//import com.auth.jwtsecurity.dto.RefreshTokenRequest;
import com.auth.jwtsecurity.dto.RegisterRequest;
import com.auth.jwtsecurity.dto.TokenPair;
import com.auth.jwtsecurity.model.User;
import com.auth.jwtsecurity.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    @Transactional
    public void registerUser(@Valid RegisterRequest registerRequest) {
        String username = registerRequest.getUsername().toLowerCase().trim();

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already in use");
        }

        if (!isStrongPassword(registerRequest.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters long, include 1 uppercase, 1 lowercase, 1 digit, and 1 special character");
        }

        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .username(username)
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())  // Should be "ADMIN", not "ROLE_ADMIN"
                .build();

        userRepository.save(user);
    }

    public TokenPair login(@Valid LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername().toLowerCase().trim(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtService.generateTokenPair(authentication);
    }

    public TokenPair refreshTokenFromCookie(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String username = jwtService.extractUsernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (userDetails == null) {
            throw new IllegalArgumentException("User not found");
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        String accessToken = jwtService.generateAccessToken(authentication);
        return new TokenPair(accessToken, refreshToken); // reusing the same refresh token
    }


    private boolean isStrongPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }


}
