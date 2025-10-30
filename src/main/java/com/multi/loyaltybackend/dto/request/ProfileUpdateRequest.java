package com.multi.loyaltybackend.dto.request;

import jakarta.validation.constraints.*;

public record ProfileUpdateRequest(
        @Size(min = 2, max = 70, message = "სრული სახელი უნდა შედგებოდეს 2-დან 70-მდე სიმბოლოსგან")
        @Pattern(
                regexp = "^[a-zA-Zა-ჰ\\s'-]+$",
                message = "სრული სახელი შეიძლება შეიცავდეს მხოლოდ ასოებს, შორისებს, დეფისებსა და აპოსტროფებს"
        )
        String fullName,

        @Min(value = 13, message = "ასაკი უნდა იყოს მინიმუმ 13")
        @Max(value = 120, message = "ასაკი არ უნდა აღემატებოდეს 120-ს")
        Integer age,

        @Pattern(
                regexp = "^$|^5\\d{8}$",
                message = "მობილური ნომერი უნდა იწყებოდეს 5-ით და შედგებოდეს 9 ციფრისგან"
        )
        String mobileNumber,

        @Size(max = 500, message = "'ჩემს შესახებ' ველი არ უნდა აღემატებოდეს 500 სიმბოლოს")
        String aboutMe
) {
}
