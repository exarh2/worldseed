package online.worldseed.service.srtm;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.config.properties.SrtmProperties;
import online.worldseed.model.entity.DemInfoEntity;
import online.worldseed.repository.DemInfoRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import static online.worldseed.model.srtm.SrtmSourceType.ARDUPILOT;

/**
 * Импорт данных из ARDUPILOT
 */
@Slf4j
@Service
public class SrtmDataArdupilotImporterService extends AbstractSrtmDataImporterService {
    private static final String ARDUPILOT_URL = "https://terrain.ardupilot.org/SRTM3/";

    public SrtmDataArdupilotImporterService(SrtmProperties srtmProperties, DemInfoRepository demInfoRepository) {
        super(srtmProperties, demInfoRepository);
    }

    @PostConstruct
    @SneakyThrows
    private void init() {
        if (!srtmProperties.getStartupImport() || srtmProperties.getSrtmImportSource() != ARDUPILOT) {
            return;
        }
        var processedArcs = new ArrayList<String>();
        processedArcs.addAll(demInfoRepository.findAllBySrtmSource(ARDUPILOT)
            .stream().map(DemInfoEntity::getArcName).distinct().toList());
        var processedRowKeys = new ArrayList<String>();
        processedRowKeys.addAll(demInfoRepository.findAllBySrtmSource(ARDUPILOT)
            .stream().map(DemInfoEntity::getRowKey).distinct().toList());
        log.info("Start importing srtm data from {}", ARDUPILOT);
        var srtmUrls = getArdupilotSrtmUrls();
        var downloadErrorCount = 0;
        for (int i = 0; i < srtmUrls.size(); i++) {
            var srtmUrl = srtmUrls.get(i);
            var arr = srtmUrl.split("/");
            var arcName = arr[arr.length - 1];
            var subFolder = arr[arr.length - 2];
            Files.createDirectories(Paths.get(srtmProperties.getSrtmArdupilotStoragePath() + subFolder));
            arcName = subFolder + "/" + arcName;
            if (processedArcs.contains(arcName)) {
                log.info((i + 1) + ". Skip download " + arcName);
            } else {
                log.info((i + 1) + ". Start downloading " + arcName);
                var arcFile = new File(srtmProperties.getSrtmArdupilotStoragePath() + arcName);
                try {
                    FileUtils.copyURLToFile(new URL(srtmUrl), arcFile);
                } catch (Exception e) {
                    downloadErrorCount++;
                    log.error(e.getMessage());
                    continue;
                }
                log.info("Downloading " + arcName + " finished");
                saveDemInfoFromArc(arcFile, arcName, processedRowKeys);
                log.info("Processing " + arcName + " finished");
                processedArcs.add(arcName);
            }
        }
        log.info("Importing srtm data success! DownloadErrorCount = {}", downloadErrorCount);
    }

    @SneakyThrows
    private List<String> getArdupilotSrtmUrls() {
        var resultSet = new HashSet<String>();
        var pattern = Pattern.compile("\\w*.hgt.zip", Pattern.CASE_INSENSITIVE);
        var subDisrs = List.of("Africa", "Antarctic", "Arctic", "Australia",
            "Eurasia", "Islands", "North_America", "South_America");
        for (var subDir : subDisrs) {
            var pageUrl = ARDUPILOT_URL + subDir + "/";
            var page = IOUtils.toString(new URL(pageUrl), StandardCharsets.UTF_8);
            var matcher = pattern.matcher(page);
            while (matcher.find()) {
                resultSet.add(pageUrl + matcher.group(0));
            }
        }
        return resultSet.stream().toList();
    }

}
