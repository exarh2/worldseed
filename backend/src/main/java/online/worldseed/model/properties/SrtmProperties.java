package online.worldseed.generator.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import online.worldseed.generator.service.srtm.model.SrtmSourceType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Конфигурация генератора
 */
@Data
@Builder
@Validated
@ConfigurationProperties("srtm")
public class SrtmProperties {
    /**
     * Текущий импортируемый источник данных
     */
    @NotNull
    private List<SrtmSourceType> srtmSourceOrder;
    /**
     * Текущий импортируемый источник данных
     */
    @NotNull
    private SrtmSourceType srtmImportSource;
    /**
     * При старте запустить импорт высотных данных из соответствующего источника
     */
    @NotNull
    private Boolean startupImport;
    /**
     * Путь к хранилищу высотных данных ardupilot
     */
    @NotBlank
    private String srtmArdupilotStoragePath;
    /**
     * Путь к хранилищу высотных данных viewfinderpanoramas
     */
    @NotBlank
    private String srtmVfpStoragePath;
    /**
     * Путь к хранилищу высотных данных CGIAR
     */
    @NotBlank
    private String srtmCgiarStoragePath;
}
