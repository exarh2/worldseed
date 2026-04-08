package online.worldseed.repository;

import online.worldseed.model.entity.TerrainEntity;
import online.worldseed.model.generator.resolution.Resolution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TerrainRepository extends JpaRepository<TerrainEntity, UUID> {
    List<TerrainEntity> findAllByResolutionIn(List<Resolution> resolutions);

    List<TerrainEntity> findAllByRowKeyIn(Set<String> rowKeyList);
}
