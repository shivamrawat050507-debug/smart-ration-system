package com.smartration.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopkeeperLoginResponse {

    private String message;
    private Long shopkeeperId;
    private String name;
}
