package online.worldseed.model.exception;

import online.worldseed.model.dto.exception.ErrorInfo;
import online.worldseed.model.dto.exception.ServiceError;
import org.springframework.lang.NonNull;

/**
 * Базовое исключение с {@link ErrorInfo}
 */
@SuppressWarnings("java:S1948")
public abstract class AbstractBaseErrorException extends RuntimeException {
    static final long serialVersionUID = 1;
    protected final ErrorInfo errorInfo;

    protected AbstractBaseErrorException(ServiceError serviceError) {
        super(serviceError.getErrorInfo().getCode() + ": " + serviceError.getErrorInfo().getMessage());
        this.errorInfo = serviceError.getErrorInfo();
    }

    protected AbstractBaseErrorException(ServiceError serviceError, Throwable cause) {
        super(serviceError.getErrorInfo().getCode() + ": " + serviceError.getErrorInfo().getMessage(), cause);
        this.errorInfo = serviceError.getErrorInfo();
    }

    protected AbstractBaseErrorException(@NonNull ErrorInfo errorInfo) {
        super(errorInfo.getCode() + ": " + errorInfo.getMessage());
        this.errorInfo = errorInfo;
    }

    protected AbstractBaseErrorException(@NonNull String message,
                                         @NonNull ErrorInfo errorInfo) {
        super(message);
        this.errorInfo = errorInfo;
    }

    protected AbstractBaseErrorException(@NonNull String message,
                                         @NonNull Throwable cause,
                                         @NonNull ErrorInfo errorInfo) {
        super(message, cause);
        this.errorInfo = errorInfo;
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }
}
