package online.worldseed.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import online.worldseed.model.enums.RoleType;
import online.worldseed.model.entity.SecurityUserEntity;
import online.worldseed.service.SecurityUserService;

@RestController
@RequestMapping("/api/users")
public class SecurityUserController {

    private final SecurityUserService service;

    public SecurityUserController(SecurityUserService service) {
        this.service = service;
    }

    @GetMapping
    public List<SecurityUserEntity> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SecurityUserEntity> getById(@PathVariable UUID id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SecurityUserEntity> create(@RequestBody Map<String, String> request) {
        String login = request.get("login");
        String password = request.get("password");
        String email = request.get("email");
        String roleStr = request.get("role");

        if (login == null || login.isBlank() ||
            password == null || password.isBlank() ||
            email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        RoleType role;
        try {
            role = roleStr != null ? RoleType.valueOf(roleStr.toUpperCase()) : RoleType.USER;
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        try {
            SecurityUserEntity created = service.create(login, password, email, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SecurityUserEntity> update(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        String login = request.get("login");
        String email = request.get("email");
        String roleStr = request.get("role");

        RoleType role = null;
        if (roleStr != null) {
            try {
                role = RoleType.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        try {
            return service.update(id, login, email, role)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<SecurityUserEntity> updatePassword(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        String password = request.get("password");
        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return service.updatePassword(id, password)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
