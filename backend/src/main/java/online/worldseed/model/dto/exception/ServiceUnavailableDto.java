package online.worldseed.model.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Тело ошибочного ответа со статусом http 503
 */
@Jacksonized
@SuperBuilder
@Schema(description = StatusDescription.HTTP_503_DESC)
public class ServiceUnavailableDto extends AbstractBaseErrorDto {
    public ServiceUnavailableDto(@NonNull String code, @NonNull String message, @Nullable List<String> reasons) {
        super(code, message, reasons);
    }
}
