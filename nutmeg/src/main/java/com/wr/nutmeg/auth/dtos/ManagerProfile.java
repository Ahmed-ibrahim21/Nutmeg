package com.wr.nutmeg.auth.dtos;

import com.wr.nutmeg.auth.ManagerUserDetails;

public record ManagerProfile(
            java.util.UUID id,
            String username,
            String email
    ) {
        public static ManagerProfile from(ManagerUserDetails details) {
            return new ManagerProfile(details.getId(), details.getUsername(), details.getEmail());
        }
    }