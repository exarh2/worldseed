package online.worldseed.service.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import online.worldseed.dto.AuthResponse;
import online.worldseed.dto.SignInRequest;
import online.worldseed.dto.SignUpRequest;
import online.worldseed.model.entity.SecurityUserEntity;
import online.worldseed.model.enums.RoleType;

@Service
public class AuthService {

    private final SecurityUserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(SecurityUserService userService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse signUp(SignUpRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        SecurityUserEntity user = userService.create(
                request.login(),
                encodedPassword,
                request.email(),
                RoleType.USER
        );
        String token = jwtService.generateToken(user.getLogin(), user.getRole().name());
        return new AuthResponse(token, user.getLogin(), user.getRole().name());
    }

    public AuthResponse signIn(SignInRequest request) {
        SecurityUserEntity user = userService.findByLogin(request.login())
                .orElseThrow(() -> new IllegalArgumentException("Invalid login or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid login or password");
        }
        String token = jwtService.generateToken(user.getLogin(), user.getRole().name());
        return new AuthResponse(token, user.getLogin(), user.getRole().name());
    }
}
