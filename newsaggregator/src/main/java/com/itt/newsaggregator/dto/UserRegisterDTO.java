package com.itt.newsaggregator.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class UserRegisterDTO {
    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
