package online.worldseed.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import online.worldseed.model.entity.SecurityUserEntity;

@Repository
public interface SecurityUserRepository extends JpaRepository<SecurityUserEntity, UUID> {

    Optional<SecurityUserEntity> findByLogin(String login);

    Optional<SecurityUserEntity> findByEmail(String email);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);
}
