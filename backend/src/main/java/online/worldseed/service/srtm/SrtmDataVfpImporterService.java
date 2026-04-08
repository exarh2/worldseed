package online.worldseed.service.srtm;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.model.entity.DemInfoEntity;
import online.worldseed.model.properties.SrtmProperties;
import online.worldseed.repository.DemInfoRepository;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static online.worldseed.model.srtm.SrtmSourceType.VFP;

/**
 * Импорт данных из viewfinderpanoramas
 */
@Slf4j
@Service
public class SrtmDataVfpImporterService extends AbstractSrtmDataImporterService {
    private static final String VFP_COVERAGE_MAP_URL = "https://www.viewfinderpanoramas.org/Coverage%20map%20viewfinderpanoramas_org3.htm";

    public SrtmDataVfpImporterService(SrtmProperties srtmProperties, DemInfoRepository demInfoRepository) {
        super(srtmProperties, demInfoRepository);
    }

    @PostConstruct
    @SneakyThrows
    private void init() {
        if (!srtmProperties.getStartupImport() || srtmProperties.getSrtmImportSource() != VFP) {
            return;
        }
        var processedArcs = new ArrayList<String>();
        processedArcs.addAll(demInfoRepository.findAllBySrtmSource(VFP)
            .stream().map(DemInfoEntity::getArcName).distinct().toList());
        var processedRowKeys = new ArrayList<String>();
        processedRowKeys.addAll(demInfoRepository.findAllBySrtmSource(VFP)
            .stream().map(DemInfoEntity::getRowKey).distinct().toList());
        log.info("Start importing srtm data from {}", VFP);
        var srtmUrls = getVfpSrtmUrls();
        var downloadErrorCount = 0;
        for (int i = 0; i < srtmUrls.size(); i++) {
            var srtmUrl = srtmUrls.get(i);
            var arr = srtmUrl.split("/");
            var arcName = arr[arr.length - 1];
            if (processedArcs.contains(arcName)) {
                log.info((i + 1) + ". Skip download " + arcName);
            } else {
                log.info((i + 1) + ". Start downloading " + arcName);
                var arcFile = new File(srtmProperties.getSrtmVfpStoragePath() + arcName);
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
    private List<String> getVfpSrtmUrls() {
        var pattern = Pattern.compile("http.*zip", Pattern.CASE_INSENSITIVE);
        var coverageMap = IOUtils.toString(new URL(VFP_COVERAGE_MAP_URL), StandardCharsets.UTF_8);
        return Arrays.stream(coverageMap.split("\n")).filter(s -> s.trim().startsWith("<area"))
            .map(s -> {
                var matcher = pattern.matcher(s);
                matcher.find();
                return matcher.group(0);
            })
            .toList();
    }
}
