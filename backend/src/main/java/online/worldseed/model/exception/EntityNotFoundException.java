package online.worldseed.generator.model.exception;

/**
 * Исключение для клиентской части с кодом ошибки 404
 */
public class EntityNotFoundException extends RuntimeException {
    static final long serialVersionUID = 1;

    public EntityNotFoundException() {
        super();
    }

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityNotFoundException(Throwable cause) {
        super(cause);
    }
}
