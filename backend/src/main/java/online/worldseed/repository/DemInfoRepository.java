package online.worldseed.generator.repository;

import online.worldseed.generator.model.entity.DemInfoEntity;
import online.worldseed.generator.service.srtm.model.SrtmSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DemInfoRepository extends JpaRepository<DemInfoEntity, UUID> {
    List<DemInfoEntity> findAllBySrtmSource(SrtmSourceType srtmSource);

    List<DemInfoEntity> findAllByRowKeyIn(List<String> rowKeys);
}
