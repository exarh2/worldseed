package online.worldseed.model.dto.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Errors implements ServiceError {
    REQUEST_VALIDATION_ERROR("WGS-0", "Ошибка валидации входных параметров"),
    LOGIN_ALREADY_EXISTS("WGS-1", "Пользователь с таким логином уже существует"),
    EMAIL_ALREADY_EXISTS("WGS-2", "Пользователь с таким email уже существует");

    private final String code;
    private final String message;

    public online.worldseed.generator.model.ErrorInfo getErrorInfo() {
        return online.worldseed.generator.model.ErrorInfo.builder()
                .code(code)
                .message(message)
                .build();
    }
}
