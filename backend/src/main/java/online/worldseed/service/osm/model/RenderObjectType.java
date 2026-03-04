package online.worldseed.service.osm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Тип рендееруемого объекта
 */
@AllArgsConstructor
@Getter
public enum RenderObjectType {
    //natural = water
    //water = lake
    //water = river
    ZERO(0, 1, List.of(), "#dddddd", "#dddddd"),
    WATER(1, 1, List.of("water", "wetland", "mud", "waterway"), "#0000ff", "#0000ff"),
    BEACH(2, 1, List.of("beach"), "#ffff00", "#ffff00"),
    WOOD(3, 1, List.of("wood"), "#00ff00", "#00ff00");

    private final int level;
    private final double smooth;
    private final List<String> names;
    private final String stroke;
    private final String fill;

    public static RenderObjectType getByName(String name) {
        return Arrays.stream(RenderObjectType.values())
                .filter(geometryType -> geometryType.getNames().contains(name)).findFirst().get();
    }

}
