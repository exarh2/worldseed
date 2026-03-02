package online.worldseed.model.dto.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Errors implements ServiceError {
    REQUEST_VALIDATION_ERROR("WS-0", "Ошибка валидации входных параметров"),
    LOGIN_ALREADY_EXISTS("WS-1", "Пользователь с таким логином уже существует"),
    EMAIL_ALREADY_EXISTS("WS-2", "Пользователь с таким email уже существует");

    private final String code;
    private final String message;

    public ErrorInfo getErrorInfo() {
        return ErrorInfo.builder()
                .code(code)
                .message(message)
                .build();
    }
}
