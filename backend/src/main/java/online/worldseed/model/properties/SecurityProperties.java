package online.worldseed.model.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Свойства конфигурации безопасности (JWT, CORS).
 */
@Validated
@Data
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    @Valid
    private final Jwt jwt = new Jwt();

    @Valid
    private final Cors cors = new Cors();

    /**
     * Настройки JWT (секрет и срок действия токена).
     */
    @Data
    public static class Jwt {

        /**
         * Секретный ключ для подписи JWT (минимум 32 символа для HS256).
         */
        @NotBlank(message = "security.jwt.secret не должен быть пустым")
        @Size(min = 32, message = "security.jwt.secret должен быть не менее 32 символов для HS256")
        private String secret = "default-secret-key-min-256-bits-for-hs256-please-set-jwt-secret-in-env";

        /**
         * Время жизни токена в миллисекундах.
         */
        @Positive(message = "security.jwt.expiration-ms должен быть положительным")
        private long expirationMs = 86400000L;
    }

    /**
     * Настройки CORS.
     */
    @Data
    public static class Cors {

        /**
         * Разрешённые источники (origins) для CORS.
         */
        @NotEmpty(message = "security.cors.allowed-origins должен содержать хотя бы один origin")
        private List<String> allowedOrigins = List.of("http://localhost:5173");
    }
}
