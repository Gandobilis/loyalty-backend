package com.multi.loyaltybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "მიმდინარე პაროლი არ შეიძლება იყოს ცარიელი")
        String currentPassword,

        @NotBlank(message = "ახალი პაროლი არ შეიძლება იყოს ცარიელი")
        @Size(min = 8, message = "ახალი პაროლი უნდა შეიცავდეს მინიმუმ 8 სიმბოლოს")
        String newPassword,

        @NotBlank(message = "პაროლის დადასტურება არ შეიძლება იყოს ცარიელი")
        String confirmPassword
) {
}
