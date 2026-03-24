package online.worldseed.generator.utils;

import lombok.SneakyThrows;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.Polygon;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Boolean.TRUE;
import static java.lang.Math.abs;
import static online.worldseed.generator.service.generator.model.GeoConstants.GD_GEOMETRY_FACTORY;
import static online.worldseed.generator.utils.GeoJsonWriter.DEBUG_DUMP_FOLDER;
import static org.locationtech.jts.geom.util.AffineTransformation.scaleInstance;
import static org.locationtech.jts.geom.util.AffineTransformation.translationInstance;

/**
 * Сохранение геометрии в виде плоского рисунка
 * используется для показа разбивки плоскости на треугольники
 */
public class GeometryImageWriter {
    private static final Integer IMAGE_RESOLUTION = 1000;

    private GeometryImageWriter() {
    }

    @SneakyThrows
    public static void convertToPlaneImage(List<Polygon> geometries, String fileName) {
        var geometryCollection = (Geometry) new GeometryCollection(geometries.toArray(Geometry[]::new), GD_GEOMETRY_FACTORY);
        var envelope = geometryCollection.getEnvelopeInternal();
        geometryCollection = translationInstance(-envelope.getMinX(), -envelope.getMinY()).transform(geometryCollection);
        var scale = envelope.getWidth() > envelope.getHeight() ?
                (IMAGE_RESOLUTION - 1) / abs(envelope.getMaxX() - envelope.getMinX()) :
                (IMAGE_RESOLUTION - 1) / abs(envelope.getMaxY() - envelope.getMinY());
        geometryCollection = scaleInstance(scale, scale).transform(geometryCollection);

        ShapeWriter sw = new ShapeWriter();
        var envelop = geometryCollection.getEnvelopeInternal();
        var image = new BufferedImage((int) envelop.getMaxX() + 1, (int) envelop.getMaxY() + 1, TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.rotate(Math.toRadians(180), envelop.getMaxX() / 2, envelop.getMaxY() / 2);
        for (int n = 0; n < geometryCollection.getNumGeometries(); n++) {
            var geometry = geometryCollection.getGeometryN(n);
            Shape polyShape = sw.toShape(geometry);
            if (TRUE.equals(geometry.getUserData())) {
                g2d.setColor(Color.red);
            } else {
                g2d.setColor(Color.blue);
            }
            g2d.draw(polyShape);
        }
        //g2d.setColor(Color.blue);
        //g2d.draw(polyShape);
        File outputFile = new File(DEBUG_DUMP_FOLDER + fileName);
        ImageIO.write(image, "jpg", outputFile);
    }
}
