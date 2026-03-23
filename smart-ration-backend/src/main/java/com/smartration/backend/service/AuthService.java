package com.smartration.backend.service;

import com.smartration.backend.dto.AuthResponse;
import com.smartration.backend.dto.RefreshTokenRequest;
import com.smartration.backend.dto.TokenRefreshResponse;
import com.smartration.backend.dto.UserLoginRequest;
import com.smartration.backend.dto.UserRegistrationRequest;

public interface AuthService {

    AuthResponse register(UserRegistrationRequest request);

    AuthResponse login(UserLoginRequest request);

    TokenRefreshResponse refreshToken(RefreshTokenRequest request);

    void invalidateRefreshToken(RefreshTokenRequest request);
}
