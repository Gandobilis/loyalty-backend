package com.multi.loyaltybackend.dto.response;

import com.multi.loyaltybackend.model.RegistrationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserEventResponse {
    private Long id;
    private String title;
    private Integer points;
    private LocalDateTime dateTime;
    private RegistrationStatus status;
}