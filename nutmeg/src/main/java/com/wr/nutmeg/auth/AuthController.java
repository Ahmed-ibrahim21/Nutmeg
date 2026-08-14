package com.wr.nutmeg.auth;

import com.wr.nutmeg.auth.dtos.LoginRequest;
import com.wr.nutmeg.auth.dtos.LoginResponse;
import com.wr.nutmeg.auth.dtos.LoginResult;
import com.wr.nutmeg.auth.dtos.ManagerProfile;
import com.wr.nutmeg.auth.dtos.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register, login, and retrieve the current manager profile")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Login",
            description = "Authenticate with username/email and password. Returns a JWT access token.",
            security = @SecurityRequirement(name = "")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content)
    })
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request.login(), request.password());
        return LoginResponse.from(result);
    }

    @Operation(
            summary = "Get current manager profile",
            description = "Returns the profile of the currently authenticated manager."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned",
                    content = @Content(schema = @Schema(implementation = ManagerProfile.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content)
    })
    @GetMapping("/me")
    public ManagerProfile me(@AuthenticationPrincipal ManagerUserDetails manager) {
        return authService.currentManager(manager);
    }

    @Operation(
            summary = "Register a new manager",
            description = "Create a new manager account. Returns a JWT access token upon successful registration.",
            security = @SecurityRequirement(name = "")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registration successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g. weak password, invalid email)",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Username or email already exists",
                    content = @Content)
    })
    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        LoginResult result = authService.register(
                request.username(), request.email(), request.password()
        );
        return LoginResponse.from(result);
    }
}
