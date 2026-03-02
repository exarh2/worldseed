package online.worldseed.model.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Базовая структура ошибочного тела ответа
 */
@Value
@NonFinal
@AllArgsConstructor
@SuperBuilder
public abstract class AbstractBaseErrorDto {
    @Schema(description = "Код ошибки")
    String code;
    @Schema(description = "Сообщение об ошибке")
    String message;
    @Schema(description = "Возможные причины ошибки")
    List<String> reasons;
}
