package online.worldseed.generator.repository;

import online.worldseed.generator.model.entity.TerrainEntity;
import online.worldseed.generator.service.generator.model.option.Resolution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TerrainRepository extends JpaRepository<TerrainEntity, UUID> {
    List<TerrainEntity> findAllByResolutionIn(List<Resolution> resolutions);

    List<TerrainEntity> findAllByRowHashIn(List<Integer> rowHashList);
}
