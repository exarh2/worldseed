package online.worldseed.model.exception;

import online.worldseed.model.dto.exception.ErrorInfo;
import online.worldseed.model.dto.exception.ServiceError;

/**
 * Исключение для клиентской части с кодом ошибки 400
 */
public class BadRequestException extends AbstractBaseErrorException {
    static final long serialVersionUID = 1;

    public BadRequestException(ServiceError serviceError) {
        super(serviceError);
    }

    public BadRequestException(ErrorInfo errorInfo) {
        super(errorInfo);
    }

    public BadRequestException(String message, ErrorInfo errorInfo) {
        super(message, errorInfo);
    }

    public BadRequestException(String message, Throwable cause, ErrorInfo errorInfo) {
        super(message, cause, errorInfo);
    }
}
