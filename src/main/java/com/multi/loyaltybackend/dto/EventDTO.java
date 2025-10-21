package com.multi.loyaltybackend.dto;

import com.multi.loyaltybackend.model.RegistrationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDTO {
    private Long id;
    private String title;
    private Integer points;
    private LocalDateTime dateTime;
    private String fileName;
    private RegistrationStatus status;
}
