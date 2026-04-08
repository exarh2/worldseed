package online.worldseed.service.generator.gltf;

import de.javagl.jgltf.model.creation.MaterialModels;
import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
import de.javagl.jgltf.model.impl.AbstractNamedModelElement;
import de.javagl.jgltf.model.impl.DefaultGltfModel;
import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultMeshPrimitiveModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import lombok.extern.slf4j.Slf4j;
import online.worldseed.model.generator.option.PlanetTerrainOptions;
import online.worldseed.model.generator.option.Resolution;
import org.springframework.stereotype.Service;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static online.worldseed.model.generator.GeoConstants.EARTH_RADIUS_A;
import static online.worldseed.model.generator.TerrainType.TERRAIN_PLANET;

@Slf4j
@Service
public class PlanetGltfModelCreator extends AbstractGltfModelCreator {
    public PlanetGltfModelCreator() {
        this.materialModelMap = Arrays.stream(Resolution.values())
                .filter(r -> r.getTerrainOptions().getGenerationType() == TERRAIN_PLANET)
                .map(r -> ((PlanetTerrainOptions) r.getTerrainOptions()).getTextureSource())
                .distinct().map(this::getMaterialModelV2)
                .collect(Collectors.toMap(AbstractNamedModelElement::getName, m -> m));
    }

    @Override
    public DefaultGltfModel createGltfFromNodeList(List<DefaultNodeModel> nodeModels) {
        var nodeModel2 = new ArrayList<>(nodeModels);
        //nodeModel2.add(createAxesNodeModel());
        return super.createGltfFromNodeList(nodeModel2);
    }

    /**
     * Создание трехмерного меша из треугольников
     */
    private DefaultNodeModel createAxesNodeModel() {
        DefaultNodeModel meshNode = new DefaultNodeModel();
        var meshModel = new DefaultMeshModel();
        meshModel.addMeshPrimitiveModel(getAxisMeshPrimitiveModel(AXES.X));
        meshModel.addMeshPrimitiveModel(getAxisMeshPrimitiveModel(AXES.Y));
        meshModel.addMeshPrimitiveModel(getAxisMeshPrimitiveModel(AXES.Z));
        meshNode.addMeshModel(meshModel);
        return meshNode;
    }

    /**
     * Создать линию оси
     */
    private DefaultMeshPrimitiveModel getAxisMeshPrimitiveModel(AXES axis) {
        var positions = new float[6];
        positions[0] = 0;
        positions[1] = 0;
        positions[2] = 0;
        positions[3] = (float) (axis == AXES.X ? 2 * EARTH_RADIUS_A : 0);
        positions[4] = (float) (axis == AXES.Y ? 2 * EARTH_RADIUS_A : 0);
        positions[5] = (float) (axis == AXES.Z ? 2 * EARTH_RADIUS_A : 0);
        MeshPrimitiveBuilder meshPrimitiveNormalBuilder = MeshPrimitiveBuilder.create();
        meshPrimitiveNormalBuilder.setLines();
        meshPrimitiveNormalBuilder.addPositions3D(FloatBuffer.wrap(positions));
        var meshPrimitiveModel = meshPrimitiveNormalBuilder.build();
        var materialModel = MaterialModels.createFromBaseColor(axis == AXES.X ? 1.0f : 0, axis == AXES.Y ? 1.0f : 0,
                axis == AXES.Z ? 1.0f : 0, 1);
        meshPrimitiveModel.setMaterialModel(materialModel);
        return meshPrimitiveModel;
    }

    private enum AXES {
        X, Y, Z
    }
}
