package online.worldseed.model.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на вход в систему")
public record SignInRequest(

    @Schema(description = "Логин пользователя", example = "user123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Login is required")
    String login,

    @Schema(description = "Пароль пользователя", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    String password
) {}
