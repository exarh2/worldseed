package online.worldseed.service.osm;

import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.geometry.RegionBuilder;
import de.topobyte.osm4j.geometry.RegionBuilderResult;
import de.topobyte.osm4j.geometry.WayBuilder;
import de.topobyte.osm4j.xml.dynsax.OsmXmlReader;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.model.geo.osm.GeometryMetaInfo;
import online.worldseed.model.geo.osm.RenderObjectType;
import online.worldseed.model.properties.GeneratorProperties;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static online.worldseed.model.geo.osm.RenderObjectType.ZERO;

@Slf4j
@Service
@RequiredArgsConstructor
public class OsmQueryService {
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private static final String QUERY = "(" +
                                        "nwr[natural~'wood|water|wetland|mud|beach'](%s);>;" +
                                        "nwr[waterway](%s);" +
                                        ");out geom;";

    private final GeneratorProperties generatorProperties;
    private WayBuilder wayBuilder = new WayBuilder();
    private RegionBuilder regionBuilder = new RegionBuilder();

    /**
     * Получение геометрий с атрибутами в заданных координатах
     * https://github.com/topobyte/osm4j-examples
     */
    @SneakyThrows
    public List<Geometry> getGeometriesFromOsm(Envelope envelope) {
        List<Geometry> geometries = new ArrayList<>();
        var bbox = (Polygon) GEOMETRY_FACTORY.toGeometry(envelope);
        InMemoryMapDataSet data = queryInMemoryMapDataSet(envelope);

        //Пути, которые уже обработаны в связях
        var relationWays = new HashSet<>();
        var wayFinder = EntityFinders.create(data, EntityNotFoundStrategy.IGNORE);

        for (OsmRelation relation : data.getRelations().valueCollection()) {
            var multiPolygon = getPolygon(relation, data);
            if (multiPolygon != null) {
                processMultiPolygonGeometry(multiPolygon.intersection(bbox), OsmModelUtil.getTagsAsMap(relation), geometries);
                wayFinder.findMemberWays(relation, (Set) relationWays);
            }
        }
        for (OsmWay way : data.getWays().valueCollection()) {
            if (relationWays.contains(way)) {
                continue;
            }
            var multiPolygon = getPolygon(way, data);
            if (multiPolygon != null) {
                processMultiPolygonGeometry(multiPolygon.intersection(bbox), OsmModelUtil.getTagsAsMap(way), geometries);
            }
        }

        intersectByLevel(bbox, GeometryMetaInfo.builder().renderObjectType(ZERO).build(), geometries);
        return geometries;
    }

    @SneakyThrows
    private InMemoryMapDataSet queryInMemoryMapDataSet(Envelope envelope) {
        var bboxStr = envelope.getMinY() + "," + envelope.getMinX() + "," + envelope.getMaxY() + "," + envelope.getMaxX();
        var query = generatorProperties.getOverpassApiUrl() + "?data=" +
                    URLEncoder.encode(QUERY.formatted(bboxStr, bboxStr), StandardCharsets.UTF_8);
        URLConnection connection = new URL(query).openConnection();
        connection.setConnectTimeout(generatorProperties.getOverpassConnectTimeoutMs());
        connection.setReadTimeout(generatorProperties.getOverpassReadTimeoutMs());
        try (InputStream input = connection.getInputStream()) {
            var reader = new OsmXmlReader(input, false);
            return MapDataSetLoader.read(reader, false, true, true);
        }
    }

    private void processMultiPolygonGeometry(Geometry multiPolygon, Map<String, String> tags, List<Geometry> geometries) {
        var metaInfo = getMetaInfo(tags);
        if (metaInfo.getRenderObjectType().getSmooth() != 1d) {
            multiPolygon = JTS.smooth(multiPolygon, metaInfo.getRenderObjectType().getSmooth());
        }
        //Сразу обрежем по boundary box для оптимизации
        intersectByLevel(multiPolygon, metaInfo, geometries);
    }

    private void intersectByLevel(Geometry geometry, GeometryMetaInfo metaInfo, List<Geometry> geometries) {
        //Вышестоящие уровни обрезают нижестоящие или текущие
        var intersections = geometries.stream().filter(g -> g.intersects(geometry)).toList();
        //При пересечении пользовательские данные затираются
        var resultGeometry = geometry;
        int level = metaInfo.getRenderObjectType().getLevel();
        for (var g : intersections) {
            var gMetaInfo = (GeometryMetaInfo) g.getUserData();
            if (gMetaInfo.getRenderObjectType().getLevel() >= level) {
                resultGeometry = resultGeometry.difference(g);
            } else {
                var updatedG = g.difference(geometry);
                updatedG.setUserData(gMetaInfo);
                geometries.remove(g);
                geometries.add(updatedG);
            }
        }
        resultGeometry.setUserData(metaInfo);
        geometries.add(resultGeometry);
    }

    private GeometryMetaInfo getMetaInfo(Map<String, String> tags) {
        final String name;
        if (tags.containsKey("waterway")) {
            name = "waterway";
        } else {
            name = tags.get("natural");
        }
        return GeometryMetaInfo.builder()
            .tags(tags)
            .renderObjectType(RenderObjectType.getByName(name))
            .build();
    }

    private MultiPolygon getPolygon(OsmWay way, OsmEntityProvider data) {
        try {
            RegionBuilderResult region = regionBuilder.build(way, data);
            return region.getMultiPolygon();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    private MultiPolygon getPolygon(OsmRelation relation, OsmEntityProvider data) {
        try {
            RegionBuilderResult region = regionBuilder.build(relation, data);
            return region.getMultiPolygon();
        } catch (EntityNotFoundException e) {
            return null;
        }
    }
}
