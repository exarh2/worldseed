package online.worldseed.generator.mapper;

import online.worldseed.generator.model.dto.scene.core.SceneTerrainOptions;
import online.worldseed.generator.model.dto.security.CustomUserDetails;
import online.worldseed.generator.model.dto.security.UserInfo;
import online.worldseed.generator.model.entity.UserEntity;
import online.worldseed.generator.service.generator.model.option.Resolution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING, imports = LocalDateTime.class)
public interface DefaultMapper {
    CustomUserDetails toCustomUserDetails(UserEntity userEntity);

    UserInfo toUserInfo(CustomUserDetails userDetails);

    @Mapping(source = "terrainOptions", target = ".")
    @Mapping(source = ".", target = "resolution")
    SceneTerrainOptions toSceneTerrainOptions(Resolution resolution);
}
