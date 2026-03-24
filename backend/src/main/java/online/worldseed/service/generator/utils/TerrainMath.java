package online.worldseed.generator.service.generator.utils;

import lombok.val;
import online.worldseed.generator.service.generator.model.Geocentric;
import online.worldseed.generator.service.generator.model.GeocentricTriangle;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.locationtech.jts.geom.Envelope;

/**
 * Математические вычисления, связанные с генерацией террейна
 */
public final class TerrainMath {
    //Масштаб генерируемых террейнов для удобства просмотра в редакторах
    private static final double TERRAIN_SCALE = 0.01;

    private TerrainMath() {
    }

    /**
     * Вычисление вектора нормали к плоскости треугольника
     */
    public static Vector3D planeNormal(GeocentricTriangle geocentricTriangle) {
        var a = geocentricTriangle.getGcCoordinates().get(0);
        var b = geocentricTriangle.getGcCoordinates().get(2);
        var c = geocentricTriangle.getGcCoordinates().get(1);
        //create two vectors that lie on the plane using these points:
        val vector1 = new Vector3D(b.getX() - a.getX(), b.getY() - a.getY(), b.getZ() - a.getZ());
        val vector2 = new Vector3D(c.getX() - a.getX(), c.getY() - a.getY(), c.getZ() - a.getZ());
        return Vector3D.crossProduct(vector1, vector2).normalize();
    }

    /**
     * Округление до 10 знаков
     */
    public static double roundAvoid(double value) {
        int places = 10;
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    /**
     * Вычисление матриц прямого и обратного преобразования террейна на плоскость XY
     * См http://algolist.manual.ru/graphics/3dfaq/articles/23.php
     */
    public static TerrainMatrices calculateTerrainMatrices(Envelope gdEnvelop) {
        //Найдем геоцентрические координаты центра террейна
        var gcCenter = Geocentric.fromGeodeticCoordinate(gdEnvelop.centre());

        //Теперь мировую систему координат надо преобразовать в локальную систему координат:
        //террейн лежит на плоскости XY,  "верх" террейна направлен по оси Y. Преобразование состоит из трех этапов:
        //1) Уменьшим масштаб в 1/ TERRAIN_SCALE раз
        val directScaleMatrix = MatrixUtils.createRealMatrix(new double[][]{
                {TERRAIN_SCALE, 0.0, 0.0, 0.0},
                {0.0, TERRAIN_SCALE, 0.0, 0.0},
                {0.0, 0.0, TERRAIN_SCALE, 0.0},
                {0.0, 0.0, 0.0, 1.0},
        });

        //2) Сдвиг террейна в центр элипсойда
        //Округление до int очень важно!!! для точности параллельного переноса
        val directTranslationMatrix = MatrixUtils.createRealMatrix(new double[][]{
                {1.0, 0.0, 0.0, -(int) gcCenter.getX()},
                {0.0, 1.0, 0.0, -(int) gcCenter.getY()},
                {0.0, 0.0, 1.0, -(int) gcCenter.getZ()},
                {0.0, 0.0, 0.0, 1.0},
        });

        //3)Совместить нормаль с осью Z
        //3) Поворот вокруг оси Z, чтобы прямая topCenter - bottomCenter проецировалась на ось Y
        //Угол поворота по вокруг оси Z определяется как угол относительно 0-го меридиана (ось X в геоцентрических координатах)
        // + 90 градусов (по часовой стрелке на плоскости XY)
        //        val psi = Math.toRadians(gdCenterCoordinate.getX() + 90);
        //        val directRotateZMatrix = MatrixUtils.createRealMatrix(new double[][]{
        //                {Math.cos(psi), Math.sin(psi), 0.0, 0.0},
        //                {-Math.sin(psi), Math.cos(psi), 0.0, 0.0},
        //                {0.0, 0.0, 1.0, 0.0},
        //                {0.0, 0.0, 0.0, 1.0}
        //        });

        //4) Угол поворота по вокруг оси X определяется через угол между вектором проходящим через точки gcTopCenter
        // и gcBottomCenter и осью Z
        //Если мы находимся в нижней полусфере, то для того, чтобы террейн не был "вниз головой" надо его довернуть на 180 градусов
        //При этом не забываем, что координаты полигона (в северном полушарии) начинаются с нижнего левого угла и идут по часовой стрелки,
        //для южного полушария - зеркально относительно экватора
        //        val gc00 = Geocentric.fromGeodeticCoordinate(gdTerrainPolygon.getCoordinates()[0]);
        //        val gc01 = Geocentric.fromGeodeticCoordinate(gdTerrainPolygon.getCoordinates()[1]);
        //        val gc11 = Geocentric.fromGeodeticCoordinate(gdTerrainPolygon.getCoordinates()[2]);
        //        val gc10 = Geocentric.fromGeodeticCoordinate(gdTerrainPolygon.getCoordinates()[3]);
        //        //Найдем координаты середины верхнего и нижнего отрезков
        //        val gcTopCenter = new Geocentric(
        //                (gc01.getX() + gc11.getX()) / 2,
        //                (gc01.getY() + gc11.getY()) / 2,
        //                (gc01.getZ() + gc11.getZ()) / 2);
        //        val gcBottomCenter = new Geocentric(
        //                (gc00.getX() + gc10.getX()) / 2,
        //                (gc00.getY() + gc10.getY()) / 2,
        //                (gc00.getZ() + gc10.getZ()) / 2);
        //        gcCenter = new Geocentric((gcTopCenter.getX() + gcBottomCenter.getX()) / 2,
        //                (gcTopCenter.getY() + gcBottomCenter.getY()) / 2,
        //                (gcTopCenter.getZ() + gcBottomCenter.getZ()) / 2);
        //
        //        val gc = new Vector3D(gcTopCenter.getX(), gcTopCenter.getY(), gcTopCenter.getZ());
        //        val centerNormal = gc.subtract(new Vector3D(gcBottomCenter.getX(), gcBottomCenter.getY(),
        //        gcBottomCenter.getZ())).normalize();
        //        val zNormal = new Vector3D(0, 0, 1).normalize();
        //        //Если мы находимся в нижней полусфере, то для того, чтобы террейн не был "вниз головой"
        //        //надо его довернуть на 180 градусов
        //        val teta = Math.PI / 2 - Vector3D.angle(centerNormal, zNormal) + ((gcCenter.getZ() < 0) ? Math.PI : 0);
        //        val directRotateXMatrix = MatrixUtils.createRealMatrix(new double[][]{
        //                {1.0, 0.0, 0.0, 0.0},
        //                {0.0, Math.cos(teta), Math.sin(teta), 0.0},
        //                {0.0, -Math.sin(teta), Math.cos(teta), 0.0},
        //                {0.0, 0.0, 0.0, 1.0},
        //        });
        //https://www.mathworks.com/matlabcentral/answers/1568718-how-can-i-rotate-vectors-so
        // -they-are-in-a-plane-defined-by-its-normal-vector
        //var normal = Geocentric.fromGeodetic(new Geodetic(gdCenterCoordinate.getY(), gdCenterCoordinate.getX(), 1000))
        //        .toVector3D().subtract(gcCenter.toVector3D()).normalize();

        //Финальная матрица прямого и обратного преобразований террейна на плоскость XY
        val directMatrix = directScaleMatrix
                //.multiply(directRotateZMatrix)
                //.multiply(directRotateXMatrix)
                .multiply(directTranslationMatrix);

        val inverseMatrix = MatrixUtils.inverse(directMatrix);
        return new TerrainMatrices(
                directMatrix,
                inverseMatrix,
                new float[]{
                        (float) inverseMatrix.getData()[0][3],
                        (float) inverseMatrix.getData()[1][3],
                        (float) inverseMatrix.getData()[2][3]},
                new float[]{
                        (float) inverseMatrix.getData()[0][0],
                        (float) inverseMatrix.getData()[1][1],
                        (float) inverseMatrix.getData()[2][2]}
        );
    }

    /**
     * Преобразование RealMatrix к линейной float матрице
     */
    @Deprecated
    public static float[] toFloatMatrix(RealMatrix realMatrix) {
        //gltf использует column-major order
        realMatrix = realMatrix.transpose();
        var matrix = new float[16];
        for (int i = 0; i < realMatrix.getData().length; i++) {
            for (int j = 0; j < realMatrix.getData()[i].length; j++) {
                matrix[i * 4 + j] = (float) realMatrix.getData()[i][j];
            }
        }
        return matrix;
    }

    public record TerrainMatrices(RealMatrix directMatrix,
                                  RealMatrix inverseMatrix,
                                  float[] inverseTranslationVector,
                                  float[] inverseScaleVector
    ) {
    }

}
