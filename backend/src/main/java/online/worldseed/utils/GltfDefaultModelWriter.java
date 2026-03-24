package online.worldseed.utils;

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.GltfModelWriter;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static online.worldseed.utils.GeoJsonWriter.DEBUG_DUMP_FOLDER;

public class GltfDefaultModelWriter {
    private static GltfModelWriter gltfModelWriter = new GltfModelWriter();

    private GltfDefaultModelWriter() {
    }

    @SneakyThrows
    public static byte[] getGltfBinary(GltfModel gltfModel) {
        var baos = new ByteArrayOutputStream();
        gltfModelWriter.writeBinary(gltfModel, baos);
        return baos.toByteArray();
    }

    @SneakyThrows
    public static void dumpGltfBinary(GltfModel gltfModel, String fileName) {
        gltfModelWriter.writeBinary(gltfModel, new File(DEBUG_DUMP_FOLDER + fileName + ".glb"));
    }

    @SneakyThrows
    public static void dumpGltf(GltfModel gltfModel, String fileName) {
        gltfModelWriter.write(gltfModel, new File(DEBUG_DUMP_FOLDER + fileName + ".json"));
    }
}
