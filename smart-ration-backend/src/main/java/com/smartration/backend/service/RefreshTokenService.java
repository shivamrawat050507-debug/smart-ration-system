package com.smartration.backend.service;

import com.smartration.backend.entity.RefreshToken;
import com.smartration.backend.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

    void deleteByUser(User user);

    void deleteByToken(String token);
}
