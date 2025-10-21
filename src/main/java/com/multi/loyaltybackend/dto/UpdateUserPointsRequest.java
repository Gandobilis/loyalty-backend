package com.multi.loyaltybackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserPointsRequest {
    @NotNull(message = "Points value is required")
    private Integer points;
}
