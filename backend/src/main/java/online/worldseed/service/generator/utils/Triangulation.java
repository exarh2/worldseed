package online.worldseed.generator.service.generator.utils;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.lang.Math.abs;
import static online.worldseed.generator.service.generator.model.GeoConstants.GD_GEOMETRY_FACTORY;

public final class Triangulation {
    //Sets the snapping tolerance which will be used to improved the robustness of the triangulation computation.
    private static final double TOLERANCE = 0.0000001;
    private static final double MIN_AREA_PERCENT = 5.0;
    //Для полярных областей, чтобы не было UV-искажений производим принудительную доп. триангуляцию
    private static final Integer POLAR_REFINEMENTS = 3;

    private Triangulation() {
    }

    public static List<Polygon> refinedTriangulation(Geometry g) {
        return refinedTriangulation(g, 0);
    }

    /**
     * https://stackoverflow.com/questions/23597434/how-to-triangulate-tesselate-some-shape-in-java
     */
    public static List<Polygon> refinedTriangulation(Geometry g, int nRefinements) {
        //Террейн по факту представляет собой полярный треугольник
        var polar = Arrays.stream(g.getCoordinates()).anyMatch(c -> abs(c.getY()) == 90);
        if (polar && nRefinements == 0) {
            nRefinements = POLAR_REFINEMENTS;
        }
        var minArea = g.getArea() * (MIN_AREA_PERCENT / 100);
        DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
        // set vertex sites
        builder.setSites(g);
        // set tolerance for initial triangulation only
        builder.setTolerance(TOLERANCE);
        // initial triangulation
        Geometry triangulation = builder.getTriangles(GD_GEOMETRY_FACTORY);

        HashSet<Coordinate> sites = new HashSet<>(Arrays.asList(triangulation.getCoordinates()));

        for (int refinement = 0; refinement < nRefinements; refinement++) {
            for (int i = 0; i < triangulation.getNumGeometries(); i++) {
                Polygon triangle = (Polygon) triangulation.getGeometryN(i);
                // skip small triangles
                if (triangle.getArea() > minArea) {
                    sites.add(new Coordinate(triangle.getCentroid().getX(), triangle.getCentroid().getY()));
                }
            }
            builder = new DelaunayTriangulationBuilder();
            builder.setSites(sites);
            // re-triangulate using new centroid sites
            triangulation = builder.getTriangles(GD_GEOMETRY_FACTORY);
        }
        // restore concave hull and any holes
        triangulation = triangulation.intersection(g);
        var result = new ArrayList<Polygon>();
        for (int i = 0; i < triangulation.getNumGeometries(); i++) {
            var triangle = (Polygon) triangulation.getGeometryN(i);
            //Среди полярных могут быть пустые треугольники (позже при преобразовании к геоцентрическим координатам)
            if (!polar || Arrays.stream(triangle.getCoordinates()).limit(3).filter(c -> abs(c.getY()) == 90).count() != 2) {
                result.add(triangle);
            }
        }
        return result;
    }
}
