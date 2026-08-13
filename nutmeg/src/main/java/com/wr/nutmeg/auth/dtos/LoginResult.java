package com.wr.nutmeg.auth.dtos;


public record LoginResult(
            String accessToken,
            String tokenType,
            long expiresInMs,
            ManagerProfile manager
    ) {}