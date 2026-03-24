package online.worldseed.generator.utils;

import lombok.SneakyThrows;
import online.worldseed.generator.service.osm.model.GeometryMetaInfo;
import org.apache.commons.io.FileUtils;
import org.locationtech.jts.geom.Geometry;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class GeoJsonWriter {
    public static final String DEBUG_DUMP_FOLDER = "c:/Projects/";

    private GeoJsonWriter() {
    }

    @SneakyThrows
    public static void dumpGeoJson(List<Geometry> geometries, String fileName) {
        GeoJSONWriter writer = new GeoJSONWriter();
        var features = geometries.stream().map(g -> {
            var propertiesMap = new HashMap<String, Object>();
            var metaInfo = (GeometryMetaInfo) g.getUserData();
            if (metaInfo != null) {
                propertiesMap.put("stroke", metaInfo.getRenderObjectType().getStroke());
                propertiesMap.put("fill", metaInfo.getRenderObjectType().getFill());
            } else {
                propertiesMap.put("stroke", "#ff00ff");
            }
            return new Feature(writer.write(g), propertiesMap);
        }).toArray(Feature[]::new);
        var featureCollection = new FeatureCollection(features);
        String json = featureCollection.toString();
        FileUtils.writeStringToFile(new File(DEBUG_DUMP_FOLDER + fileName), json, StandardCharsets.UTF_8);
    }
}
