package com.wr.nutmeg.auth;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wr.nutmeg.auth.dtos.LoginRequest;
import com.wr.nutmeg.auth.dtos.LoginResponse;
import com.wr.nutmeg.auth.dtos.LoginResult;
import com.wr.nutmeg.auth.dtos.ManagerProfile;
import com.wr.nutmeg.auth.dtos.RegisterRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request.login(), request.password());
        return LoginResponse.from(result);
    }

    @GetMapping("/me")
    public ManagerProfile me(@AuthenticationPrincipal ManagerUserDetails manager) {
        return authService.currentManager(manager);
    }

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
     LoginResult result = authService.register(
            request.username(), request.email(), request.password()
      );
    return LoginResponse.from(result);
    }
}
