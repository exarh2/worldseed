package online.worldseed.model.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.jackson.Jacksonized;
import org.springframework.lang.Nullable;

/**
 * Тело ошибочного ответа со статусом http 500
 */
@Value
@Builder
@NonFinal
@Jacksonized
@RequiredArgsConstructor
@Schema(description = StatusDescription.HTTP_500_DESC)
public class ServiceErrorDto {
    /**
     * Описание причины ошибки
     */
    @Nullable
    @Schema(description = "Описание причины ошибки")
    String error;
}
