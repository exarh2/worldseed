package online.worldseed.repository;

import online.worldseed.model.entity.SecurityUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecurityUserRepository extends JpaRepository<SecurityUserEntity, UUID> {

    Optional<SecurityUserEntity> findByLogin(String login);

    Optional<SecurityUserEntity> findByEmail(String email);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);
}
