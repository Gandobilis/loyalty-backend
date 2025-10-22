package com.multi.loyaltybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventFilterDTO {
    private String title;
    private String category;
    private String address;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Integer minPoints;
    private Integer maxPoints;
    private Boolean hasLocation;
}
