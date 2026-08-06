package com.wr.nutmeg.api;

import com.wr.nutmeg.auth.AuthService;
import com.wr.nutmeg.auth.ManagerUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
