package com.wr.nutmeg.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResult result = authService.login(request.login(), request.password());
        return LoginResponse.from(result);
    }

    @GetMapping("/me")
    public AuthService.ManagerProfile me(@AuthenticationPrincipal ManagerUserDetails manager) {
        return authService.currentManager(manager);
    }

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
     AuthService.LoginResult result = authService.register(
            request.username(), request.email(), request.password()
      );
    return LoginResponse.from(result);
    }

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters") String password
) {
}

    public record LoginRequest(
            @NotBlank String login,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(
            String accessToken,
            String tokenType,
            long expiresInMs,
            AuthService.ManagerProfile manager
    ) {
        static LoginResponse from(AuthService.LoginResult result) {
            return new LoginResponse(
                    result.accessToken(),
                    result.tokenType(),
                    result.expiresInMs(),
                    result.manager()
            );
        }
    }
}
