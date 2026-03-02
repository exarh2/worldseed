package online.worldseed.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.model.exception.ServiceErrorException;
import online.worldseed.model.properties.MinioProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioProperties.getHost(), minioProperties.getPort(), minioProperties.getSecure())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();

        log.info("MinioClient was initialized - host: {}, port: {}, secure: {} ",
                minioProperties.getHost(), minioProperties.getPort(), minioProperties.getSecure());

        minioClient.setTimeout(
                minioProperties.getConnectTimeout(),
                minioProperties.getWriteTimeout(),
                minioProperties.getReadTimeout()
        );
        try {
            boolean isBucketExist = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.getTerrainsBucketName())
                    .build());
            if (!isBucketExist) {
                try {
                    minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(minioProperties.getTerrainsBucketName())
                            .build());
                    log.info("Bucket successfully created: {}", minioProperties.getTerrainsBucketName());
                } catch (Exception e) {
                    log.error("Can't create bucket: {}", minioProperties.getTerrainsBucketName(), e);
                    throw new ServiceErrorException("Can't create bucket: " + e.getMessage());
                }
            } else {
                log.info("Bucket already created: {}", minioProperties.getTerrainsBucketName());
            }
        } catch (Exception e) {
            log.error("Error checking bucket exist: {}", minioProperties.getTerrainsBucketName(), e);
            throw new ServiceErrorException("Error checking bucket exist: " + e.getMessage());
        }
        return minioClient;
    }
}
