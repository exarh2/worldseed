package online.worldseed.generator.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import online.worldseed.generator.service.generator.model.option.Resolution;
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
@Table(name = "terrain")
@EntityListeners(AuditingEntityListener.class)
public class TerrainEntity {
    /**
     * Идентификатор записи
     */
    @Id
    @NotNull
    private UUID id;
    /**
     * Настройки разрешения
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private Resolution resolution;
    /**
     * Поисковый индекс (lat + "_" + lon)
     */
    @NotNull
    private String rowKey;
    /**
     * Хэш поискового индекса
     */
    @NotNull
    private Integer rowHash;
    /**
     * Путь в хранилище
     */
    @NotNull
    private String storagePath;
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
