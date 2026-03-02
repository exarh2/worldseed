package online.worldseed.generator.model.exception;

import online.worldseed.generator.model.ErrorInfo;
import online.worldseed.generator.model.ServiceError;

/**
 * Исключение для клиентской части с кодом ошибки 422
 */
public class BusinessProcessException extends AbstractBaseErrorException {
    static final long serialVersionUID = 1;

    public BusinessProcessException(ServiceError serviceError) {
        super(serviceError);
    }

    public BusinessProcessException(ServiceError serviceError, Throwable cause) {
        super(serviceError, cause);
    }

    public BusinessProcessException(String message,
                                    ErrorInfo errorInfo) {
        super(message, errorInfo);
    }

    public BusinessProcessException(String message,
                                    Throwable cause,
                                    ErrorInfo errorInfo) {
        super(message, cause, errorInfo);
    }
}
