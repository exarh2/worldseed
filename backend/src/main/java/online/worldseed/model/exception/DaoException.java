package online.worldseed.generator.model.exception;

/**
 * Исключение для бизнесовой части, не обрабатывается на клиентской части,
 * используйте когда произошла проблема с загрузкой данных
 */
public class DaoException extends RuntimeException {
    static final long serialVersionUID = 1;

    public DaoException() {
        super();
    }

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoException(Throwable cause) {
        super(cause);
    }
}
