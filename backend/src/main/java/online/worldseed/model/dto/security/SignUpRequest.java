package online.worldseed.model.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на регистрацию нового пользователя")
public record SignUpRequest(

    @Schema(description = "Логин пользователя", example = "user123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Login is required")
    @Size(min = 1, max = 50)
    String login,

    @Schema(description = "Пароль пользователя", example = "secret123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 4, max = 100)
    String password,

    @Schema(description = "Адрес электронной почты", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email
    @Size(max = 255)
    String email
) { }
