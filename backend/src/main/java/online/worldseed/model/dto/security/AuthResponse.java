package online.worldseed.model.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с данными аутентификации")
public record AuthResponse(
    @Schema(description = "JWT-токен для авторизации запросов") String token,
    @Schema(description = "Логин пользователя") String login,
    @Schema(description = "Роль пользователя в системе") String role
) {}
