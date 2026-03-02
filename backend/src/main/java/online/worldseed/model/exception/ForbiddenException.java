package online.worldseed.generator.model.exception;

/**
 * Исключение для клиентской части с кодом ошибки 403
 */
public class ForbiddenException extends RuntimeException {
    static final long serialVersionUID = 1;

    public ForbiddenException() {
    }

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenException(Throwable cause) {
        super(cause);
    }
}
