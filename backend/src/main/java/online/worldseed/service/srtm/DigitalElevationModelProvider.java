package online.worldseed.service.srtm;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.model.entity.DemInfoEntity;
import online.worldseed.model.properties.SrtmProperties;
import online.worldseed.repository.DemInfoRepository;
import org.locationtech.jts.geom.Envelope;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static online.worldseed.model.srtm.StrmConstants.DEM3_RESOLUTION;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigitalElevationModelProvider {
    private final SrtmProperties srtmProperties;
    private final DemInfoRepository demInfoRepository;

    public DigitalElevationModel loadDemForArea(Envelope bounds) {
        log.debug("Start creating DigitalElevationModel for " + bounds);
        var digitalElevationModel = new DigitalElevationModel(bounds);

        var rowKeys = getRowKeys(bounds);
        var demInfoMap = getDemInfoMap(rowKeys);

        for (var rowKey : rowKeys) {
            if (demInfoMap != null && demInfoMap.containsKey(rowKey)) {
                log.info("Start importing from DemInfoEntity {} ", rowKey);
                var demInfo = demInfoMap.get(rowKey);
                copyElevationPointsFrom(digitalElevationModel, demInfo.getLat(), demInfo.getLon(),
                        Optional.of(demInfoMap.get(rowKey)));
            } else {
                //log.info("Not found DemInfoEntity for {}, set zero data", rowKey);
                var lat = Integer.parseInt(rowKey.split("_")[0]);
                var lon = Integer.parseInt(rowKey.split("_")[1]);
                copyElevationPointsFrom(digitalElevationModel, lat, lon, Optional.empty());
            }
        }
        var stat = digitalElevationModel.getStatistics();
        log.debug("DigitalElevationModel created, min: {}, max: {}, missing: {}",
                stat.getMinElevation(), stat.getMaxElevation(), stat.getMissingPoints());
        return digitalElevationModel;
    }

    public Envelope getDemOutboundEnvelop(Envelope bounds, int gridSize) {
        var latStep = (bounds.getMaxY() - bounds.getMinY()) / gridSize;
        var lonStep = (bounds.getMaxX() - bounds.getMinX()) / gridSize;
        return new Envelope(
                bounds.getMinX() - 3 * lonStep, bounds.getMaxX() + lonStep,
                bounds.getMinY() - 3 * latStep, bounds.getMaxY() + latStep);
    }

    public List<String> getRowKeys(Envelope bounds) {
        var rowKeys = new ArrayList<String>();
        for (int lat = calcCellDegrees(bounds.getMinY()); lat <= calcCellDegrees(bounds.getMaxY()) && lat != 90; lat++) {
            for (int lon = calcCellDegrees(bounds.getMinX()); lon <= calcCellDegrees(bounds.getMaxX()) && lon != 180; lon++) {
                //Ищем в районе 180 меридиана - делаем проброс
                //var realLon = lon == 180 ? lon - 360 : (lon == -181 ? lon + 360 : lon);
                //rowKeys.add(lat + "_" + realLon);
                rowKeys.add(lat + "_" + lon);
            }
        }
        log.debug("Found {} rowKeys to process", rowKeys.size());
        return rowKeys;
    }

    private Map<String, DemInfoEntity> getDemInfoMap(List<String> rowKeys) {
        var demSrtmSourceMap = demInfoRepository.findAllByRowKeyIn(rowKeys)
                .stream().collect(Collectors.groupingBy(DemInfoEntity::getSrtmSource));

        Map<String, DemInfoEntity> demInfoMap = null;
        //Расставляем приоритеты источников данных
        if (!demSrtmSourceMap.isEmpty()) {
            List<DemInfoEntity> demInfoList = demSrtmSourceMap.get(srtmProperties.getSrtmSourceOrder().get(0));
            if (demInfoList == null) {
                demInfoList = demSrtmSourceMap.get(srtmProperties.getSrtmSourceOrder().get(1));
            }
            if (demInfoList == null) {
                demInfoList = demSrtmSourceMap.get(srtmProperties.getSrtmSourceOrder().get(2));
            }
            if (demInfoList != null) {
                log.info("User source {} for: {}", demInfoList.get(0).getSrtmSource(), rowKeys);
                demInfoMap = demInfoList.stream().collect(Collectors.toMap(DemInfoEntity::getRowKey, d -> d));
            }
        }
        return demInfoMap;
    }

    @SneakyThrows
    private void copyElevationPointsFrom(DigitalElevationModel dem, int lat, int lon, Optional<DemInfoEntity> demInfo) {
        // cell absolute position
        int cellLatAbs = lat * DEM3_RESOLUTION;
        int cellLonAbs = lon * DEM3_RESOLUTION;

        // first find the place to copy to and extent
        // initialize the copying rectangle to the cell's extent
        var west = cellLonAbs;
        var south = cellLatAbs;
        var east = west + DEM3_RESOLUTION - 1;
        var north = south + DEM3_RESOLUTION - 1;

        // now intersect it with the destination extent
        if (dem.getWest() > west) {
            west = dem.getWest();
        }
        if (dem.getSouth() > south) {
            south = dem.getSouth();
        }
        if (dem.getWest() + dem.getWidth() - 1 < east) {
            east = dem.getWest() + dem.getWidth() - 1;
        }
        if (dem.getSouth() + dem.getHeight() - 1 < north) {
            north = dem.getSouth() + dem.getHeight() - 1;
        }

        if (demInfo.isEmpty()) {
            for (int yy = north; yy >= south; yy--) {
                for (int xx = west; xx <= east; xx++) {
                    dem.setElevationForDataPoint(xx - dem.getWest(), yy - dem.getSouth(), (short) 0);
                }
            }
        } else {
            var storagePath = switch (demInfo.get().getSrtmSource()) {
                case CGIAR -> srtmProperties.getSrtmCgiarStoragePath();
                case VFP -> srtmProperties.getSrtmVfpStoragePath();
                case ARDUPILOT -> srtmProperties.getSrtmArdupilotStoragePath();
            };
            var arcFile = new File(storagePath + demInfo.get().getArcName());
            try (var zipFile = new ZipFile(arcFile)) {
                Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
                while (zipEntries.hasMoreElements()) {
                    ZipEntry zipEntry = zipEntries.nextElement();
                    if (zipEntry.getName().equals(demInfo.get().getFilePath())) {
                        try (InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry))) {
                            var dataInputStream = new DataInputStream(inputStream);
                            dataInputStream.skipBytes((int) ((DEM3_RESOLUTION - (north - cellLatAbs)) * (DEM3_RESOLUTION + 1) * 2L));
                            //var read = (DEM3_RESOLUTION - (north - cellLatAbs)) * (DEM3_RESOLUTION + 1) * 2L;
                            for (int yy = north; yy >= south; yy--) {
                                dataInputStream.skipBytes((int) ((west - cellLonAbs) * 2L));
                                //read += (west - cellLonAbs) * 2L;
                                for (int xx = west; xx <= east; xx++) {
                                    var elevation = dataInputStream.readShort();
                                    //read += 2;
                                    dem.setElevationForDataPoint(xx - dem.getWest(), yy - dem.getSouth(), (short) elevation);
                                }
                                dataInputStream.skipBytes((int) ((DEM3_RESOLUTION - (east - cellLonAbs)) * 2L));
                                //read += (DEM3_RESOLUTION - (east - cellLonAbs)) * 2L;
                            }
                            //System.out.println(read);
                        }
                    }
                }
            }
        }
    }

    private int calcCellDegrees(double angle) {
        if (angle >= 0) {
            return (int) angle;
        }
        return (int) Math.floor(angle);
    }
}
