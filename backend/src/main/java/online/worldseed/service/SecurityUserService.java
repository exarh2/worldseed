package online.worldseed.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import online.worldseed.model.enums.RoleType;
import online.worldseed.model.entity.SecurityUserEntity;
import online.worldseed.repository.SecurityUserRepository;

@Service
public class SecurityUserService {

    private final SecurityUserRepository repository;

    public SecurityUserService(SecurityUserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SecurityUserEntity> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<SecurityUserEntity> findById(UUID id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<SecurityUserEntity> findByLogin(String login) {
        return repository.findByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<SecurityUserEntity> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Transactional
    public SecurityUserEntity create(String login, String password, String email, RoleType role) {
        if (repository.existsByLogin(login)) {
            throw new IllegalArgumentException("User with login " + login + " already exists");
        }
        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        SecurityUserEntity user = new SecurityUserEntity();
        user.setId(UUID.randomUUID());
        user.setLogin(login);
        user.setPassword(password);
        user.setEmail(email);
        user.setRole(role);
        return repository.save(user);
    }

    @Transactional
    public Optional<SecurityUserEntity> update(UUID id, String login, String email, RoleType role) {
        return repository.findById(id)
                .map(user -> {
                    if (login != null && !login.isBlank() && !login.equals(user.getLogin())) {
                        if (repository.existsByLogin(login)) {
                            throw new IllegalArgumentException("User with login " + login + " already exists");
                        }
                        user.setLogin(login);
                    }
                    if (email != null && !email.isBlank() && !email.equals(user.getEmail())) {
                        if (repository.existsByEmail(email)) {
                            throw new IllegalArgumentException("User with email " + email + " already exists");
                        }
                        user.setEmail(email);
                    }
                    if (role != null) {
                        user.setRole(role);
                    }
                    return repository.save(user);
                });
    }

    @Transactional
    public Optional<SecurityUserEntity> updatePassword(UUID id, String password) {
        return repository.findById(id)
                .map(user -> {
                    user.setPassword(password);
                    return repository.save(user);
                });
    }

    @Transactional
    public boolean delete(UUID id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
