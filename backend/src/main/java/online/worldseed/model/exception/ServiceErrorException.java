package online.worldseed.generator.model.exception;

/**
 * Исключение для клиентской части с кодом ошибки 500
 */
public class ServiceErrorException extends RuntimeException {
    static final long serialVersionUID = 1;

    private final String error;

    public ServiceErrorException(String message) {
        super(message);
        this.error = message;
    }

    public ServiceErrorException(String message, Throwable cause) {
        super(message, cause);
        this.error = message;
    }

    public ServiceErrorException(String error,
                                 String message) {
        super(message);
        this.error = error;
    }

    public ServiceErrorException(String error,
                                 String message,
                                 Throwable cause) {
        super(message, cause);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
