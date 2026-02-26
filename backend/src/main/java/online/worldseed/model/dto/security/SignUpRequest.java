package online.worldseed.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(

    @NotBlank(message = "Login is required")
    @Size(min = 1, max = 50)
    String login,

    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 100)
    String password,

    @NotBlank(message = "Email is required")
    @Email
    @Size(max = 255)
    String email
) {}
