package online.worldseed.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import online.worldseed.model.dto.security.AuthResponse;
import online.worldseed.model.dto.security.SignInRequest;
import online.worldseed.model.dto.security.SignUpRequest;
import online.worldseed.service.security.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public AuthResponse signUp(@Valid @RequestBody SignUpRequest request) {
        return authService.signUp(request);
    }

    @PostMapping("/sign-in")
    public AuthResponse signIn(@Valid @RequestBody SignInRequest request) {
        return authService.signIn(request);
    }
}
