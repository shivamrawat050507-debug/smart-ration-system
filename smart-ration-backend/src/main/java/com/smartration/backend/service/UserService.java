package com.smartration.backend.service;

import com.smartration.backend.dto.UserResponse;

public interface UserService {

    UserResponse getUserById(Long userId);
}
