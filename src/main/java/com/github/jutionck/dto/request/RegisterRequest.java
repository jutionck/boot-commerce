package com.github.jutionck.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    private String phone;

    @NotBlank
    private String password;

    @NotBlank
    private String confirmPassword;

    private String role; // Optional: CUSTOMER, SELLER (defaults to CUSTOMER)
}
