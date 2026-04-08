package online.worldseed.service.srtm;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import online.worldseed.model.dto.exception.ErrorInfo;
import online.worldseed.model.entity.DemInfoEntity;
import online.worldseed.model.exception.ServiceUnavailableException;
import online.worldseed.model.properties.SrtmProperties;
import online.worldseed.repository.DemInfoRepository;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferShort;
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;

import static java.awt.image.BufferedImage.TYPE_USHORT_GRAY;
import static online.worldseed.model.srtm.SrtmSourceType.CGIAR;
import static online.worldseed.model.srtm.StrmConstants.DEM3_RESOLUTION;

/**
 * Импорт данных из CGIAR
 */
@Slf4j
@Service
public class SrtmDataCgiarImporterService extends AbstractSrtmDataImporterService {
    private static final String CGIAR_URL = "https://srtm.csi.cgiar.org/wp-content/uploads/files/srtm_5x5/TIFF/";
    private static final String RAW_DATA_DIR = "raw/";
    private static final Pattern RAW_ARC_FILE_PATTERN = Pattern.compile("srtm_(\\d\\d)_(\\d\\d).zip", Pattern.CASE_INSENSITIVE);

    public SrtmDataCgiarImporterService(SrtmProperties srtmProperties, DemInfoRepository demInfoRepository) {
        super(srtmProperties, demInfoRepository);
    }

    @PostConstruct
    @SneakyThrows
    private void init() {
        if (!srtmProperties.getStartupImport() || srtmProperties.getSrtmImportSource() != CGIAR) {
            return;
        }
        //Скачивание сырых данных (tif 5x5), если еще не скачены
        if (!downloadGeotiffRawData()) {
            throw new ServiceUnavailableException("Download geotiff raw data error", ErrorInfo.builder().build());
        }
        //Конвертация в hgt-архив (25 dem-файлов)
        Stream.of(new File(srtmProperties.getSrtmCgiarStoragePath() + RAW_DATA_DIR).listFiles())
                .filter(f -> !f.isDirectory() &&
                        f.getName().startsWith("srtm_") && f.getName().endsWith(".zip"))
                .map(File::getName)
                .filter(rawArcFileName -> !new File(srtmProperties.getSrtmCgiarStoragePath() +
                        rawArcFileName.replace("srtm_", "hgt_")).exists())
                .forEach(this::convertFromGeotiffRawData);
        importToDB();
    }

    @SneakyThrows
    private boolean downloadGeotiffRawData() {
        log.info("Start downloading srtm data from {}", CGIAR);
        var downloadErrorCount = 0;

        if (!new File(srtmProperties.getSrtmCgiarStoragePath() + RAW_DATA_DIR + "srtm_72_22.zip").exists()) {
            for (int lat = 1; lat <= 24; lat++) {
                for (int lon = 1; lon <= 72; lon++) {
                    var fileName = "srtm_" + String.format("%02d", lon) +
                            "_" + String.format("%02d", lat) + ".zip";
                    var arcFile = new File(srtmProperties.getSrtmCgiarStoragePath() + RAW_DATA_DIR + fileName);
                    if (!arcFile.exists()) {
                        var srtmUrl = new URL(CGIAR_URL + fileName);
                        var huc = (HttpURLConnection) srtmUrl.openConnection();
                        huc.setRequestMethod("HEAD");
                        int responseCode = huc.getResponseCode();
                        if (responseCode != 404) {
                            log.info("Start downloading " + srtmUrl);
                            try {
                                FileUtils.copyURLToFile(srtmUrl, arcFile);
                            } catch (Exception e) {
                                downloadErrorCount++;
                                log.error(e.getMessage());
                                continue;
                            }
                        }
                    }
                }
            }
        }
        log.info("Download raw srtm data success! DownloadErrorCount = {}", downloadErrorCount);
        return downloadErrorCount == 0;
    }

    @SneakyThrows
    private void convertFromGeotiffRawData(String rawArcFileName) {
        log.info("Start converting to hgt arc with 25 dems {}", rawArcFileName);
        var tiffFileName = rawArcFileName.replace(".zip", ".tif");
        try (var zipFile = new ZipFile(new File(srtmProperties.getSrtmCgiarStoragePath() + RAW_DATA_DIR + rawArcFileName))) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                if (zipEntry.getName().equals(tiffFileName)) {
                    try (InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry))) {
                        var tiffImage = ImageIO.read(inputStream);
                        convertToHgtArc(rawArcFileName, tiffImage);
                        log.info("Success converting to hgt arc with 25 dems {}", rawArcFileName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error converting to hgt arc{}", rawArcFileName);
        }
    }

    private void importToDB() {
        log.info("Start importing srtm data from {} data folder", CGIAR);
        var processedArcs = new ArrayList<String>();
        processedArcs.addAll(demInfoRepository.findAllBySrtmSource(CGIAR)
                .stream().map(DemInfoEntity::getArcName).distinct().toList());
        var processedRowKeys = new ArrayList<String>();
        processedRowKeys.addAll(demInfoRepository.findAllBySrtmSource(CGIAR)
                .stream().map(DemInfoEntity::getRowKey).distinct().toList());
        Stream.of(new File(srtmProperties.getSrtmCgiarStoragePath()).listFiles())
                .filter(f -> !f.isDirectory() &&
                        f.getName().startsWith("hgt_") && f.getName().endsWith(".zip"))
                .filter(f -> !processedArcs.contains(f.getName()))
                .forEach(f -> this.saveDemInfoFromArc(f, f.getName(), processedRowKeys));

        log.info("Import srt data success");
    }

    @SneakyThrows
    private void convertToHgtArc(String rawArcFileName, BufferedImage image) {
        var hgtArcFileName = srtmProperties.getSrtmCgiarStoragePath() + rawArcFileName.replace("srtm_", "hgt_");
        var matcher = RAW_ARC_FILE_PATTERN.matcher(rawArcFileName);
        if (!matcher.find()) {
            log.warn("Wrong raw arc file name: {}", rawArcFileName);
            return;
        }
        var minLat = 60 - Integer.parseInt(matcher.group(2)) * 5;
        var minLon = -180 + (Integer.parseInt(matcher.group(1)) - 1) * 5;
        var ns = minLat >= 0 ? 'N' : 'S';
        var we = minLon >= 0 ? 'E' : 'W';
        //6000*6000 (5*5 градусов) разделяем на пять частей 1200*1200
        try (val fos = new FileOutputStream(hgtArcFileName); val zos = new ZipOutputStream(fos)) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    var pixels = (short[]) image.getData()
                            .getDataElements(DEM3_RESOLUTION * j, DEM3_RESOLUTION * i,
                                    DEM3_RESOLUTION, DEM3_RESOLUTION, null);
                    //Если квадрат 1 x 1 без данных, то смысла сохранять нет
                    var hasData = false;
                    for (var p : pixels) {
                        if (p != Short.MIN_VALUE) {
                            hasData = true;
                            break;
                        }
                    }
                    if (hasData) {
                        var hgtBytes = convertToHgtBytes(pixels);
                        var hgtFileName = ns + String.format("%02d", Math.abs(minLat + 4 - i)) +
                                we + String.format("%03d", Math.abs(minLon + j)) + ".hgt";
                        //dumpHgtToImage(hgtBytes, hgtFileName.replace(".hgt", ".tif"));
                        val ze = new ZipEntry(hgtFileName);
                        zos.putNextEntry(ze);
                        for (byte b : hgtBytes) {
                            zos.write(b);
                        }
                    }
                }
            }
        }
    }

    private byte[] convertToHgtBytes(short[] pixels) {
        //Сверху и справа добавляем фейковую склейку с NO_DATA (Short.MIN_VALUE)
        var result = new byte[2 * (DEM3_RESOLUTION + 1) * (DEM3_RESOLUTION + 1)];
        int idx = 0;
        for (int n = 0; n < DEM3_RESOLUTION + 1; n++) {
            result[idx++] = (byte) (Short.MIN_VALUE >> 8);
            result[idx++] = (byte) Short.MIN_VALUE;
        }
        for (int n = 0; n < pixels.length; n++) {
            if (n > 0 && n % DEM3_RESOLUTION == 0) {
                result[idx++] = (byte) (Short.MIN_VALUE >> 8);
                result[idx++] = (byte) Short.MIN_VALUE;
            }
            result[idx++] = (byte) (pixels[n] >> 8);
            result[idx++] = (byte) pixels[n];
        }
        result[idx++] = (byte) (Short.MIN_VALUE >> 8);
        result[idx++] = (byte) Short.MIN_VALUE;
        return result;
    }

    @SneakyThrows
    private void dumpHgtToImage(byte[] hgtBytes, String fileName) {
        var pixels = new short[hgtBytes.length / 2];
        for (int i = 0; i < hgtBytes.length / 2; i++) {
            pixels[i] = (short) (((hgtBytes[2 * i] & 0xFF) << 8) | (hgtBytes[2 * i + 1] & 0xFF));
        }
        var subImage = new BufferedImage(DEM3_RESOLUTION + 1, DEM3_RESOLUTION + 1, TYPE_USHORT_GRAY);
        Raster raster = Raster.createRaster(subImage.getSampleModel(), new DataBufferShort(pixels, pixels.length), null);
        subImage.setData(raster);
        File outputFile = new File(fileName);
        ImageIO.write(subImage, "tiff", outputFile);
    }
}
