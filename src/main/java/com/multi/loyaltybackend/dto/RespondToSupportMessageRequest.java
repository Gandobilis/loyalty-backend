package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.SupportMessageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespondToSupportMessageRequest {

    @NotBlank(message = "Response is required")
    @Size(max = 5000, message = "Response must not exceed 5000 characters")
    private String response;

    // Optional - update status when responding
    private SupportMessageStatus status;
}
