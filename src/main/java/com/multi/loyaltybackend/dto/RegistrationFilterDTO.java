package com.multi.loyaltybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationFilterDTO {
    private String userEmail;
    private String userName;
    private String eventTitle;
    private Long eventId;
    private String status;
    private LocalDate registeredFrom;
    private LocalDate registeredTo;
}
