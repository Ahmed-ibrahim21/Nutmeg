package com.wr.nutmeg.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public LoginResult login(String login, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.trim(), password)
        );

        ManagerUserDetails managerDetails = (ManagerUserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(managerDetails.getId(), managerDetails.getUsername());

        return new LoginResult(
                accessToken,
                "Bearer",
                jwtProperties.getExpirationMs(),
                ManagerProfile.from(managerDetails)
        );
    }

    public ManagerProfile currentManager(ManagerUserDetails managerDetails) {
        return ManagerProfile.from(managerDetails);
    }

    public record LoginResult(
            String accessToken,
            String tokenType,
            long expiresInMs,
            ManagerProfile manager
    ) {
    }

    public record ManagerProfile(
            java.util.UUID id,
            String username,
            String email
    ) {
        static ManagerProfile from(ManagerUserDetails details) {
            return new ManagerProfile(details.getId(), details.getUsername(), details.getEmail());
        }
    }
}
