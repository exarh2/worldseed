package online.worldseed.model.properties;

import lombok.Builder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация minio
 */
@Data
@Builder
@Validated
@ConfigurationProperties("minio")
public class MinioProperties {
    /**
     * Minio host
     */
    private String host;
    /**
     * Minio port
     */
    private Integer port;
    /**
     * Minio secure
     */
    private Boolean secure;
    /**
     * Логин
     */
    private String accessKey;
    /**
     * Пароль
     */
    private String secretKey;
    /**
     * Имя корзины
     */
    private String terrainsBucketName;
    /**
     * Таймаут на доступ, ms
     */
    private long connectTimeout;
    /**
     * Таймаут на запись
     */
    private long writeTimeout;
    /**
     * Таймаут на чтение
     */
    private long readTimeout;
}
