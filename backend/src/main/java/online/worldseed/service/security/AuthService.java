package online.worldseed.service.security;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import online.worldseed.model.dto.security.AuthResponse;
import online.worldseed.model.dto.security.SignInRequest;
import online.worldseed.model.dto.security.SignUpRequest;
import online.worldseed.model.entity.SecurityUserEntity;
import static online.worldseed.model.enums.RoleType.USER;
import online.worldseed.model.enums.RoleType;
import online.worldseed.repository.SecurityUserRepository;

@Service
@RequiredArgsConstructorg
public class AuthService {

    private final SecurityUserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Регистрирует нового пользователя: проверяет уникальность логина и email,
     * сохраняет пользователя с закодированным паролем и возвращает JWT и данные для входа.
     */
    public AuthResponse signUp(SignUpRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());

        if (userRepository.existsByLogin(request.login())) {
            throw new IllegalArgumentException("User with login " + request.login() + " already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("User with email " + request.email() + " already exists");
        }

        SecurityUserEntity user = new SecurityUserEntity();
        user.setId(UUID.randomUUID());
        user.setLogin(request.login());
        user.setPassword(encodedPassword);
        user.setEmail(request.email());
        user.setRole(USER);

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getLogin(), user.getRole().name());
        return new AuthResponse(token, user.getLogin(), user.getRole().name());
    }

    /**
     * Выполняет вход пользователя: проверяет логин и пароль, возвращает JWT и данные пользователя.
     */
    public AuthResponse signIn(SignInRequest request) {
        SecurityUserEntity user = userRepository.findByLogin(request.login())
                .orElseThrow(() -> new IllegalArgumentException("Invalid login or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid login or password");
        }
        String token = jwtService.generateToken(user.getLogin(), user.getRole().name());
        return new AuthResponse(token, user.getLogin(), user.getRole().name());
    }
}
