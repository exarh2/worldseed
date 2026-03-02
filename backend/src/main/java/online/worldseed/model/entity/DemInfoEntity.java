package online.worldseed.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.worldseed.model.enums.SrtmSourceType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dem_info")
@EntityListeners(AuditingEntityListener.class)
public class DemInfoEntity {
    /**
     * Идентификатор записи
     */
    @NotNull
    @Id
    private UUID id;
    /**
     * Источник высотных данных
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SrtmSourceType srtmSource;
    /**
     * Широта
     */
    @NotNull
    private Integer lat;
    /**
     * Долгота
     */
    @NotNull
    private Integer lon;
    /**
     * Поисковый индекс (lat + "_" + lon)
     */
    @NotBlank
    private String rowKey;
    /**
     * Название файла архива
     */
    @NotBlank
    private String arcName;
    /**
     * Путь файла в архиве
     */
    @NotBlank
    private String filePath;
    /**
     * Дата создания записи
     */
    @NotNull
    @CreatedDate
    private Instant createdDate;
    /**
     * Дата изменения записи
     */
    @NotNull
    @LastModifiedDate
    private Instant lastModifiedDate;
}
