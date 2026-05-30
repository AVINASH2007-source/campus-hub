package com.college.events.service;

import com.college.events.dto.auth.*;
import com.college.events.entity.User;
import com.college.events.exception.BadRequestException;
import com.college.events.exception.ConflictException;
import com.college.events.exception.ResourceNotFoundException;
import com.college.events.repository.UserRepository;
import com.college.events.security.CustomUserDetails;
import com.college.events.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already registered: " + req.email());
        }
        if (req.rollNumber() != null && !req.rollNumber().isBlank()
                && userRepository.existsByRollNumber(req.rollNumber())) {
            throw new ConflictException("Roll number already in use: " + req.rollNumber());
        }
        if (!req.password().equals(req.confirmPassword())) {
            throw new BadRequestException("Passwords do not match.");
        }

        User user = User.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .rollNumber(req.rollNumber())
                .department(req.department())
                .phone(req.phone())
                .role(User.Role.STUDENT)
                .enabled(true)
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .build();

        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken  = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String accessToken  = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
        return buildAuthResponse(userDetails.getUser(), accessToken, refreshToken);
    }

    public TokenRefreshResponse refreshToken(TokenRefreshRequest req) {
        String email = jwtTokenProvider.extractUsername(req.refreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
        CustomUserDetails userDetails = new CustomUserDetails(user);
        if (!jwtTokenProvider.isTokenValid(req.refreshToken(), userDetails)) {
            throw new BadRequestException("Refresh token is invalid or expired.");
        }
        String newAccess  = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefresh = jwtTokenProvider.generateRefreshToken(userDetails);
        return new TokenRefreshResponse(newAccess, newRefresh);
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token."));
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    @Transactional
    public void sendPasswordResetEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPasswordResetToken(UUID.randomUUID().toString());
            user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            log.info("Password reset requested for: {}", email);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        User user = userRepository.findByPasswordResetToken(req.token())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token."));
        if (user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired.");
        }
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                new AuthResponse.UserDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getDepartment(),
                        user.getRollNumber()
                )
        );
    }
}