package com.multi.loyaltybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterDTO {
    private String email;
    private String fullName;
    private String role;
    private Integer minPoints;
    private Integer maxPoints;
    private LocalDate createdFrom;
    private LocalDate createdTo;
}
