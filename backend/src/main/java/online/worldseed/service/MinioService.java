package online.worldseed.service;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.config.properties.MinioProperties;
import online.worldseed.model.exception.ServiceErrorException;
import online.worldseed.model.generator.resolution.Resolution;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import static online.worldseed.model.generator.TerrainType.TERRAIN_PLANET;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    /**
     * Сохранение террейна в минио
     */
    public String saveTerrain(UUID terrainId, Resolution resolution, Coordinate center, byte[] gltfBinary) {
        var terrainStorageFilePath = getTerrainStorageFilePath(resolution, center, terrainId);
        var contentType = "model/gltf-binary";
        try {
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioProperties.getTerrainsBucketName())
                .object(terrainStorageFilePath)
                .stream(new ByteArrayInputStream(gltfBinary), gltfBinary.length, -1)
                .contentType(contentType)
                .build());
        } catch (Exception e) {
            log.error("Minio error while saving file {} to bucket {} with contentType {}",
                terrainStorageFilePath, minioProperties.getTerrainsBucketName(), contentType, e);
            throw new ServiceErrorException("Minio error while saving file " + terrainStorageFilePath + " to bucket " +
                                            minioProperties.getTerrainsBucketName() + " with contentType " + contentType, e);
        }
        return terrainStorageFilePath;
    }

    public byte[] getTerrainBinary(String storagePath) {
        try (var stream = minioClient.getObject(GetObjectArgs.builder()
            .bucket(minioProperties.getTerrainsBucketName())
            .object(storagePath)
            .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new ServiceErrorException("Minio error while reading file " + storagePath + " from bucket " +
                                            minioProperties.getTerrainsBucketName(), e);
        }
    }

    public void overwriteTerrainBinary(String storagePath, byte[] gltfBinary) {
        var contentType = "model/gltf-binary";
        try {
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioProperties.getTerrainsBucketName())
                .object(storagePath)
                .stream(new ByteArrayInputStream(gltfBinary), gltfBinary.length, -1)
                .contentType(contentType)
                .build());
        } catch (Exception e) {
            throw new ServiceErrorException("Minio error while overwriting file " + storagePath + " to bucket " +
                                            minioProperties.getTerrainsBucketName() + " with contentType " + contentType, e);
        }
    }

    /**
     * Очистка всего бакета террейнов
     */
    @SneakyThrows
    public void clearTerrainBucket() {
        for (var itemResult : minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(minioProperties.getTerrainsBucketName())
                .recursive(true)
                .build()
        )) {
            minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(minioProperties.getTerrainsBucketName())
                .object(itemResult.get().objectName())
                .build());
        }
    }

    /**
     * Получение относительного пути хранения террейна в хранилище
     */
    private String getTerrainStorageFilePath(Resolution resolution, Coordinate center, UUID terrainId) {
        var lon = center.getX();
        var lat = center.getY();
        var path = resolution.name().toLowerCase() + "/";
        if (resolution.getTerrainOptions().getGenerationType() != TERRAIN_PLANET) {
            path += (int) Math.floor(lon) + "/" + (int) Math.floor(lat) + "/";
        }
        var subSplit = 1 / resolution.getTerrainOptions().getLatStep() / 32;
        if (subSplit >= 1) {
            path += (int) (subSplit * Math.abs(lon % 1)) + "/";
            path += (int) (subSplit * Math.abs(lat % 1)) + "/";
        }
        path += terrainId + ".glb";
        return path;
    }
}
