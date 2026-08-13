package com.wr.nutmeg.manager.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AssignClubRequest(
            @NotNull UUID clubId
    ) {}
