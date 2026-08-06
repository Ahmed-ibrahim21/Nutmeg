package com.wr.nutmeg.auth;

import com.wr.nutmeg.manager.ManagerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ManagerUserDetailsService implements UserDetailsService {

    private final ManagerRepository managerRepository;

    public ManagerUserDetailsService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) {
        return managerRepository.findByUsernameOrEmail(login.trim())
                .map(ManagerUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Manager not found"));
    }

    public ManagerUserDetails loadUserByManagerId(UUID managerId) {
        return managerRepository.findById(managerId)
                .map(ManagerUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Manager not found"));
    }
}
