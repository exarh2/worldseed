package online.worldseed.service.srtm;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.config.properties.SrtmProperties;
import online.worldseed.model.entity.DemInfoEntity;
import online.worldseed.repository.DemInfoRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static online.worldseed.model.srtm.StrmConstants.DEM3_FILE_SIZE;

/**
 * Импорт данных из соответствующего источника
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractSrtmDataImporterService {
    private static final Pattern DEM_FILE_PATTERN = Pattern.compile("([NS])(\\d\\d)([EW])(\\d\\d\\d).hgt", Pattern.CASE_INSENSITIVE);

    protected final SrtmProperties srtmProperties;
    protected final DemInfoRepository demInfoRepository;

    @SneakyThrows
    protected void saveDemInfoFromArc(File arcFile, String arcName, ArrayList<String> processedRowKeys) {
        try (var zipFile = new ZipFile(arcFile)) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                if (zipEntry.getName().endsWith(".hgt")) {
                    if (zipEntry.getSize() != DEM3_FILE_SIZE) {
                        log.warn("Unknown hgt file size! My be file corrupted");
                        continue;
                    }
                    var matcher = DEM_FILE_PATTERN.matcher(zipEntry.getName());
                    if (!matcher.find()) {
                        log.warn("Wrong hgt file name: {}", zipEntry.getName());
                        continue;
                    }
                    var ns = matcher.group(1);
                    var lat = Integer.parseInt(matcher.group(2));
                    var ew = matcher.group(3);
                    var lon = Integer.parseInt(matcher.group(4));

                    lat = "N".equals(ns) ? lat : -lat;
                    lon = "E".equals(ew) ? lon : -lon;
                    var rowKey = lat + "_" + lon;
                    if (!processedRowKeys.contains(rowKey)) {
                        var demInfoEntity = DemInfoEntity.builder()
                            .id(UUID.randomUUID())
                            .srtmSource(srtmProperties.getSrtmImportSource())
                            .lat(lat)
                            .lon(lon)
                            .rowKey(rowKey)
                            .arcName(arcName)
                            .filePath(zipEntry.getName())
                            .build();
                        demInfoRepository.save(demInfoEntity);
                        processedRowKeys.add(rowKey);
                    } else {
                        log.warn("Skip processed rowKey {}", rowKey);
                    }
                }
            }
        }
    }
}
