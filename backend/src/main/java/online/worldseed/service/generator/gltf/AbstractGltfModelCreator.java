package online.worldseed.service.generator.gltf;

import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.creation.GltfModelBuilder;
import de.javagl.jgltf.model.creation.MaterialBuilder;
import de.javagl.jgltf.model.creation.MaterialModels;
import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
import de.javagl.jgltf.model.impl.DefaultGltfModel;
import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultMeshPrimitiveModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.model.generator.Geocentric;
import online.worldseed.model.generator.GeocentricTriangle;
import online.worldseed.service.generator.utils.TerrainMath;
import org.springframework.stereotype.Service;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public abstract class AbstractGltfModelCreator {
    protected Map<String, MaterialModelV2> materialModelMap = new HashMap<>();
    //private MaterialModelV2 baseMaterialModel;

    @SneakyThrows
    public MaterialModelV2 getBaseMaterialModel() {
        var baseMaterialModel = MaterialModels.createFromBaseColor(
            (float) Math.random(),
            (float) Math.random(),
            (float) Math.random(),
            0.8f);
        baseMaterialModel.setDoubleSided(false);
        return baseMaterialModel;
    }

    /**
     * Сохранение gltf с одной нодой
     */
    public DefaultGltfModel createGltfFromNode(DefaultNodeModel nodeModel) {
        return createGltfFromNodeList(List.of(nodeModel));
    }

    /**
     * Сохранение gltf со списком нод
     */
    public DefaultGltfModel createGltfFromNodeList(List<DefaultNodeModel> nodeModels) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        DefaultNodeModel nodeParent = new DefaultNodeModel();
        nodeParent.setName("parent_node");
        sceneModel.addNode(nodeParent);
        nodeModels.forEach(nodeParent::addChild);

        GltfModelBuilder gltfModelBuilder = GltfModelBuilder.create();
        gltfModelBuilder.addSceneModel(sceneModel);

        return gltfModelBuilder.build();
    }

    /**
     * Создание ноды сцены вместе с мешами
     *
     * @see <a href="https://github.com/KhronosGroup/glTF/blob/main/specification/1.0/schema/node.schema.json">Схема gltf</a>
     */
    public DefaultNodeModel createNodeModel(List<GeocentricTriangle> triangles, Optional<String> textureSource,
                                            TerrainMath.TerrainMatrices terrainMatrices) {
        DefaultNodeModel meshNode = new DefaultNodeModel();
        // A node can have either a `matrix` or any combination of `translation`/`rotation`/`scale` (TRS) properties.
        // TRS properties are converted to matrices and postmultiplied in the `T * R * S` order to compose the transformation matrix;
        // first the scale is applied to the vertices, then the rotation, and then the translation. If none are provided,
        // the transform is the identity.
        //meshNode.setMatrix(toFloatMatrix(terrainMatrices.inverseMatrix()));
        meshNode.setTranslation(terrainMatrices.inverseTranslationVector());
        meshNode.setScale(terrainMatrices.inverseScaleVector());
        meshNode.addMeshModel(createMeshModel(triangles, getMaterialModel(textureSource)));
        return meshNode;
    }

    /**
     * Создание трехмерного меша из треугольников
     */
    private MeshModel createMeshModel(List<GeocentricTriangle> triangles, MaterialModelV2 materialModel) {
        //Построим список уникальных координат
        var uniqueCoords = triangles.stream().map(GeocentricTriangle::getGcCoordinates).flatMap(Collection::stream)
            .collect(Collectors.toSet()).stream().toList();
        var hasUv = uniqueCoords.get(0).getUv().isPresent();

        var positions = new float[uniqueCoords.size() * 3];
        var normals = new float[uniqueCoords.size() * 3];
        var texCoords0 = hasUv ? new float[uniqueCoords.size() * 2] : null;
        for (int i = 0; i < uniqueCoords.size(); i++) {
            var coordinate = uniqueCoords.get(i);

            positions[i * 3] = (float) coordinate.getX();
            positions[i * 3 + 1] = (float) coordinate.getY();
            positions[i * 3 + 2] = (float) coordinate.getZ();

            normals[i * 3] = (float) coordinate.getNormal().get().getX();
            normals[i * 3 + 1] = (float) coordinate.getNormal().get().getY();
            normals[i * 3 + 2] = (float) coordinate.getNormal().get().getZ();

            if (hasUv) {
                texCoords0[i * 2] = coordinate.getUv().get().getFirst();
                texCoords0[i * 2 + 1] = coordinate.getUv().get().getSecond();
            }
        }
        var indices = new int[triangles.size() * 3];
        for (int i = 0; i < triangles.size(); i++) {
            var triangleCoordinates = triangles.get(i);
            indices[i * 3] = uniqueCoords.indexOf(triangleCoordinates.getGcCoordinates().get(0));
            indices[i * 3 + 1] = uniqueCoords.indexOf(triangleCoordinates.getGcCoordinates().get(2));
            indices[i * 3 + 2] = uniqueCoords.indexOf(triangleCoordinates.getGcCoordinates().get(1));
        }

        var meshPrimitiveBuilder = MeshPrimitiveBuilder.create();
        meshPrimitiveBuilder.addPositions3D(FloatBuffer.wrap(positions));
        meshPrimitiveBuilder.setIntIndices(IntBuffer.wrap(indices));
        //Закомментировать, если надо видеть треугольники
        meshPrimitiveBuilder.addNormals3D(FloatBuffer.wrap(normals));
        if (hasUv) {
            meshPrimitiveBuilder.addTexCoords02D(FloatBuffer.wrap(texCoords0));
        }
        var meshPrimitiveModel = meshPrimitiveBuilder.build();
        meshPrimitiveModel.setMaterialModel(materialModel);

        var meshModel = new DefaultMeshModel();
        meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
        //meshModel.addMeshPrimitiveModel(getNormalMeshPrimitiveModel(uniqueCoords));
        return meshModel;
    }

    /**
     * Получить материал для меша
     */
    private MaterialModelV2 getMaterialModel(Optional<String> textureSource) {
        return textureSource.map(s -> materialModelMap.get(s))
            .orElse(getBaseMaterialModel());
    }

    /**
     * Создать меш с линиями нормалей
     */
    private DefaultMeshPrimitiveModel getNormalMeshPrimitiveModel(List<Geocentric> uniqueCoords) {
        var positions = new float[uniqueCoords.size() * 6];
        for (int i = 0; i < uniqueCoords.size(); i++) {
            var coordinate = uniqueCoords.get(i);
            var normal = coordinate.getNormal().get().scalarMultiply(0.8);
            positions[i * 6] = (float) coordinate.getX();
            positions[i * 6 + 1] = (float) coordinate.getY();
            positions[i * 6 + 2] = (float) coordinate.getZ();
            positions[i * 6 + 3] = (float) (coordinate.getX() + normal.getX());
            positions[i * 6 + 4] = (float) (coordinate.getY() + normal.getY());
            positions[i * 6 + 5] = (float) (coordinate.getZ() + normal.getZ());
        }
        MeshPrimitiveBuilder meshPrimitiveNormalBuilder = MeshPrimitiveBuilder.create();
        meshPrimitiveNormalBuilder.setLines();
        meshPrimitiveNormalBuilder.addPositions3D(FloatBuffer.wrap(positions));
        var meshPrimitiveModel = meshPrimitiveNormalBuilder.build();
        var materialModel = MaterialModels.createFromBaseColor(
            (float) Math.random(),
            (float) Math.random(),
            (float) Math.random(),
            0.9f);
        meshPrimitiveModel.setMaterialModel(materialModel);
        return meshPrimitiveModel;
    }

    @SneakyThrows
    protected MaterialModelV2 getMaterialModelV2(String textureSource) {
        var materialBuilder = MaterialBuilder.create();
        var fileName = getClass().getClassLoader().getResource("textures/" + textureSource).toURI().toString();
        materialBuilder.setBaseColorTexture(fileName, textureSource, null);
        materialBuilder.setDoubleSided(false);
        var materialModel = materialBuilder.build();
        materialModel.setName(textureSource);
        return materialModel;
    }
}
