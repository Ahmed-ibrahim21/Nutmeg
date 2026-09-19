package com.wr.nutmeg.manager;

import com.wr.nutmeg.auth.ManagerUserDetails;
import com.wr.nutmeg.manager.dtos.AssignClubRequest;
import com.wr.nutmeg.manager.dtos.AssignmentResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/managers")
@Tag(name = "Manager", description = "Manager account operations and club assignment")
public class ManagerController {

    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @Operation(
            summary = "Assign a club to the manager",
            description = "Assigns an unmanaged club to the authenticated manager. A manager can only manage one club at a time."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Club assigned successfully",
                    content = @Content(schema = @Schema(implementation = AssignmentResult.class))),
            @ApiResponse(responseCode = "404", description = "Manager or club not found",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Manager already manages a club, or club already has a manager",
                    content = @Content)
    })
    @PostMapping("/assign-club")
    public AssignmentResult assignClub(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @Valid @RequestBody AssignClubRequest request
    ) {
        return managerService.assignClub(principal.getId(), request.clubId());
    }
}

