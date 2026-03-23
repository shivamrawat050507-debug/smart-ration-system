package com.smartration.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShopkeeperLoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
