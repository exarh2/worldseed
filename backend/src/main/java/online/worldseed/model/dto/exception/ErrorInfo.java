package online.worldseed.model.dto.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Информация об ошибке
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorInfo implements Serializable {
    static final long serialVersionUID = 1;
    /**
     * Код ошибки
     */
    private String code;
    /**
     * Заголовок ошибки
     */
    private String message;
}
