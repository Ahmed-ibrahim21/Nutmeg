package com.wr.nutmeg.auth.dtos;


public record LoginResponse(
            String accessToken,
            String tokenType,
            long expiresInMs,
            ManagerProfile manager
    ) {
        public static LoginResponse from(LoginResult result) {
            return new LoginResponse(
                    result.accessToken(),
                    result.tokenType(),
                    result.expiresInMs(),
                    result.manager()
            );
        }
    }