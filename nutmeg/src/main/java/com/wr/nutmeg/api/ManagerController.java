package com.wr.nutmeg.api;

import com.wr.nutmeg.auth.ManagerUserDetails;
import com.wr.nutmeg.manager.ManagerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/managers")
public class ManagerController {

    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @PostMapping("/assign-club")
    public ManagerService.AssignmentResult assignClub(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @Valid @RequestBody AssignClubRequest request
    ) {
        return managerService.assignClub(principal.getId(), request.clubId());
    }

    public record AssignClubRequest(
            @NotNull UUID clubId
    ) {}
}
