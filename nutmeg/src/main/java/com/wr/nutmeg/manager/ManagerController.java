package com.wr.nutmeg.manager;

import com.wr.nutmeg.auth.ManagerUserDetails;
import com.wr.nutmeg.manager.dtos.AssignClubRequest;
import com.wr.nutmeg.manager.dtos.AssignmentResult;
import com.wr.nutmeg.tactics.TacticsService;
import com.wr.nutmeg.tactics.dtos.SetTacticsRequest;
import com.wr.nutmeg.tactics.dtos.TacticsResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/managers")
public class ManagerController {

    private final ManagerService managerService;
    private final TacticsService tacticsService;

    public ManagerController(ManagerService managerService, TacticsService tacticsService) {
        this.managerService = managerService;
        this.tacticsService = tacticsService;
    }

    @PostMapping("/assign-club")
    public AssignmentResult assignClub(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @Valid @RequestBody AssignClubRequest request
    ) {
        return managerService.assignClub(principal.getId(), request.clubId());
    }

    @GetMapping("/tactics")
    public TacticsResponse getTactics(
            @AuthenticationPrincipal ManagerUserDetails principal
    ) {
        return tacticsService.getTactics(principal.getId());
    }

    @PutMapping("/tactics")
    public TacticsResponse updateTactics(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @Valid @RequestBody SetTacticsRequest request
    ) {
        return tacticsService.updateTactics(principal.getId(), request);
    }
}
