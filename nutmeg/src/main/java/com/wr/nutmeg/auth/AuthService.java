package com.wr.nutmeg.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wr.nutmeg.auth.dtos.LoginResult;
import com.wr.nutmeg.auth.dtos.ManagerProfile;
import com.wr.nutmeg.manager.Manager;
import com.wr.nutmeg.manager.ManagerRepository;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties,
            ManagerRepository managerRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
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

    public LoginResult register(String username, String email, String rawPassword) {
    if (managerRepository.findByUsernameOrEmail(username.trim()).isPresent()
            || managerRepository.findByUsernameOrEmail(email.trim()).isPresent()) {
        throw new IllegalStateException("Username or email already in use");
    }

    Manager manager = new Manager();
    manager.setUsername(username.trim());
    manager.setEmail(email.trim());
    manager.setPasswordHash(passwordEncoder.encode(rawPassword));

    managerRepository.save(manager);

    return login(username.trim(), rawPassword);
}

    public ManagerProfile currentManager(ManagerUserDetails managerDetails) {
        return ManagerProfile.from(managerDetails);
    }

  


}
