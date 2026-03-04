package online.worldseed.generator.service.osm.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class GeometryMetaInfo {
    //Тип рендееруемого объекта
    private RenderObjectType renderObjectType;
    //Теги
    private Map<String, String> tags;
}
