package online.worldseed.generator.model.exception;

/**
 * Исключение для клиентской части с кодом ошибки 401
 */
public class UnauthorizedException extends RuntimeException {
    static final long serialVersionUID = 1;

    public UnauthorizedException() {
    }

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
    }
}
