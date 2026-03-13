package com.cedarco.service;

import com.cedarco.dto.AuthDto;
import com.cedarco.entity.User;
import com.cedarco.repository.UserRepository;
import com.cedarco.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthDto.TokenResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.CUSTOMER)
                .active(true)
                .addresses(new java.util.ArrayList<>())
                .build();
        userRepository.save(user);
        return buildTokenResponse(user);
    }

    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return buildTokenResponse(user);
    }

    public AuthDto.TokenResponse refresh(AuthDto.RefreshRequest request) {
        String token = request.getRefreshToken();
        if (!jwtUtil.isTokenValid(token)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return buildTokenResponse(user);
    }

    private AuthDto.TokenResponse buildTokenResponse(User user) {
        String access = jwtUtil.generateToken(user.getEmail());
        String refresh = jwtUtil.generateRefreshToken(user.getEmail());
        return new AuthDto.TokenResponse(user.getId(), access, refresh, user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getRole().name());
    }
}
