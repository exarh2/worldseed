package online.worldseed.model.dto;

import jakarta.validation.constraints.NotBlank;

public record SignInRequest(

    @NotBlank(message = "Login is required")
    String login,

    @NotBlank(message = "Password is required")
    String password
) {}
