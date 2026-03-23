package com.smartration.backend.service.impl;

import com.smartration.backend.dto.UserResponse;
import com.smartration.backend.entity.User;
import com.smartration.backend.exception.ResourceNotFoundException;
import com.smartration.backend.repository.UserRepository;
import com.smartration.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .rationCardNumber(user.getRationCardNumber())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }
}
