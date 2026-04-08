package online.worldseed.mapper;

import online.worldseed.model.dto.scene.core.SceneTerrainOptions;
import online.worldseed.model.generator.option.Resolution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, imports = LocalDateTime.class)
public interface DefaultMapper {
    //TODO    CustomUserDetails toCustomUserDetails(UserEntity userEntity);
    //
    //    UserInfo toUserInfo(CustomUserDetails userDetails);
    //
    @Mapping(source = "terrainOptions", target = ".")
    @Mapping(source = ".", target = "resolution")
    SceneTerrainOptions toSceneTerrainOptions(Resolution resolution);
}
