package online.worldseed.model.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Тело ошибочного ответа со статусом http 400
 */
@Jacksonized
@SuperBuilder
@Schema(description = StatusDescription.HTTP_400_DESC)
public class BadRequestDto extends AbstractBaseErrorDto {
    public BadRequestDto(@NonNull String code, @NonNull String message, @Nullable List<String> reasons) {
        super(code, message, reasons);
    }
}
