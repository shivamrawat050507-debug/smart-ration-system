package com.smartration.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginRequest {

    @NotBlank(message = "Ration card number is required")
    private String rationCardNumber;

    @NotBlank(message = "Password is required")
    private String password;
}
