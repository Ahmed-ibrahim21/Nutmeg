package com.wr.nutmeg.auth;

import com.wr.nutmeg.manager.Manager;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class ManagerUserDetails implements UserDetails {

    private final UUID id;
    private final String username;
    private final String email;
    private final String passwordHash;

    public ManagerUserDetails(Manager manager) {
        this.id = manager.getId();
        this.username = manager.getUsername();
        this.email = manager.getEmail();
        this.passwordHash = manager.getPasswordHash();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_MANAGER"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
