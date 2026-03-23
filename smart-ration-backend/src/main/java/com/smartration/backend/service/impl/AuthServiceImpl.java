package com.smartration.backend.service.impl;

import com.smartration.backend.dto.AuthResponse;
import com.smartration.backend.dto.RefreshTokenRequest;
import com.smartration.backend.dto.TokenRefreshResponse;
import com.smartration.backend.entity.RefreshToken;
import com.smartration.backend.dto.UserLoginRequest;
import com.smartration.backend.dto.UserRegistrationRequest;
import com.smartration.backend.entity.Role;
import com.smartration.backend.entity.User;
import com.smartration.backend.exception.BadRequestException;
import com.smartration.backend.repository.UserRepository;
import com.smartration.backend.security.JwtService;
import com.smartration.backend.service.AuthService;
import com.smartration.backend.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponse register(UserRegistrationRequest request) {
        if (userRepository.existsByRationCardNumber(request.getRationCardNumber())) {
            throw new BadRequestException("Ration card number already exists");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone number already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .rationCardNumber(request.getRationCardNumber())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);
        org.springframework.security.core.userdetails.UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(
                        savedUser.getRationCardNumber(),
                        savedUser.getPassword(),
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(savedUser.getRole().name()))
                );
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(userDetails))
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .rationCardNumber(savedUser.getRationCardNumber())
                .role(savedUser.getRole())
                .message("Registration successful")
                .build();
    }

    @Override
    public AuthResponse login(UserLoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getRationCardNumber(), request.getPassword())
        );

        User user = userRepository.findByRationCardNumber(request.getRationCardNumber())
                .orElseThrow(() -> new BadRequestException("Invalid ration card number or password"));

        org.springframework.security.core.userdetails.UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getRationCardNumber(),
                        user.getPassword(),
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().name()))
                );
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(userDetails))
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .rationCardNumber(user.getRationCardNumber())
                .role(user.getRole())
                .message("Login successful")
                .build();
    }

    @Override
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();

        org.springframework.security.core.userdetails.UserDetails userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getRationCardNumber(),
                        user.getPassword(),
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(user.getRole().name()))
                );

        return TokenRefreshResponse.builder()
                .accessToken(jwtService.generateAccessToken(userDetails))
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .build();
    }

    @Override
    public void invalidateRefreshToken(RefreshTokenRequest request) {
        refreshTokenService.deleteByToken(request.getRefreshToken());
    }
}
