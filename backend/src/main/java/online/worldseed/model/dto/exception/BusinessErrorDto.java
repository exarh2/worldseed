package online.worldseed.model.dto.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Тело ошибочного ответа со статусом http 422
 */
@Jacksonized
@SuperBuilder
@Schema(description = StatusDescription.HTTP_422_DESC)
public class BusinessErrorDto extends AbstractBaseErrorDto {
    public BusinessErrorDto(@NonNull String code, @NonNull String message, @Nullable List<String> reasons) {
        super(code, message, reasons);
    }
}
