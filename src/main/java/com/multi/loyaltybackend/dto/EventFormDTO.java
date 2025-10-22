package com.multi.loyaltybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventFormDTO {
    private Long id;
    private String title;
    private String shortDescription;
    private String description;
    private String category;
    private String address;
    private Double latitude;
    private Double longitude;
    private LocalDateTime dateTime;
    private Integer points;
}
