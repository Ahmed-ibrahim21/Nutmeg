package com.wr.nutmeg.manager.dtos;

import java.util.UUID;

public record AssignmentResult(
            UUID managerId,
            String managerUsername,
            UUID clubId,
            String clubName
    ) {}
