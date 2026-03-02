package online.worldseed.repository;

import online.worldseed.model.entity.DemInfoEntity;
import online.worldseed.model.enums.SrtmSourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DemInfoRepository extends JpaRepository<DemInfoEntity, UUID> {
    List<DemInfoEntity> findAllBySrtmSource(SrtmSourceType srtmSource);

    List<DemInfoEntity> findAllByRowKeyIn(List<String> rowKeys);
}
