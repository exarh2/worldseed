package online.worldseed.generator.model.exception;

import online.worldseed.generator.model.ErrorInfo;

/**
 * Исключение для клиентской части с кодом ошибки 503 запроса стороннего сервиса
 */
public class ServiceUnavailableException extends AbstractBaseErrorException {
    static final long serialVersionUID = 1;

    public ServiceUnavailableException(String message, ErrorInfo errorInfo) {
        super(message, errorInfo);
    }

    public ServiceUnavailableException(String message, Throwable cause, ErrorInfo errorInfo) {
        super(message, cause, errorInfo);
    }
}
