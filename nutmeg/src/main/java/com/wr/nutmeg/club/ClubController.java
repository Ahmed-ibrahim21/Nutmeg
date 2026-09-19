package com.wr.nutmeg.club;

import com.wr.nutmeg.auth.ManagerUserDetails;
import com.wr.nutmeg.club.dtos.LineupResponse;
import com.wr.nutmeg.club.dtos.SetLineupRequest;
import com.wr.nutmeg.exceptions.InvlaidStateException;
import com.wr.nutmeg.exceptions.ResourceNotFoundException;
import com.wr.nutmeg.manager.Manager;
import com.wr.nutmeg.manager.ManagerRepository;
import com.wr.nutmeg.tactics.TacticsService;
import com.wr.nutmeg.tactics.dtos.SetTacticsRequest;
import com.wr.nutmeg.tactics.dtos.TacticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequestMapping("/api/clubs/my")
@Tag(name = "Club", description = "Tactics and starting lineup management for the authenticated manager's club")
public class ClubController {

    private final ManagerRepository managerRepository;
    private final TacticsService tacticsService;
    private final LineupService lineupService;

    public ClubController(ManagerRepository managerRepository, TacticsService tacticsService, LineupService lineupService) {
        this.managerRepository = managerRepository;
        this.tacticsService = tacticsService;
        this.lineupService = lineupService;
    }

    @Operation(
            summary = "Get current tactics",
            description = "Returns the tactical configuration of the manager's club, including the coherence score."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tactics returned",
                    content = @Content(schema = @Schema(implementation = TacticsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Manager or club not found",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet",
                    content = @Content)
    })
    @GetMapping("/tactics")
    public TacticsResponse getTactics(
            @AuthenticationPrincipal ManagerUserDetails principal
    ) {
        UUID clubId = resolveClubId(principal);
        return tacticsService.getTactics(clubId);
    }

    @Operation(
            summary = "Update tactics",
            description = "Updates the tactical configuration (formation, game plan, pressing, etc.). If the formation changes, the starting lineup is automatically rebuilt."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tactics updated",
                    content = @Content(schema = @Schema(implementation = TacticsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet",
                    content = @Content)
    })
    @PutMapping("/tactics")
    public TacticsResponse updateTactics(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @Valid @RequestBody SetTacticsRequest request
    ) {
        UUID clubId = resolveClubId(principal);
        return tacticsService.updateTactics(clubId, request);
    }

    @Operation(
            summary = "Get starting lineup",
            description = "Returns the current starting XI for the manager's club, including player details for each formation slot."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lineup returned",
                    content = @Content(schema = @Schema(implementation = LineupResponse.class))),
            @ApiResponse(responseCode = "404", description = "Manager or club not found",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet",
                    content = @Content)
    })
    @GetMapping("/lineup")
    public LineupResponse getLineup(
            @AuthenticationPrincipal ManagerUserDetails principal
    ) {
        UUID clubId = resolveClubId(principal);
        return lineupService.getLineup(clubId);
    }

    @Operation(
            summary = "Update starting lineup",
            description = "Replaces the starting XI. Requires exactly 11 assignments matching the current formation's slots. All players must belong to the manager's club and be available (not injured or suspended)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lineup updated",
                    content = @Content(schema = @Schema(implementation = LineupResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error (wrong slot count, duplicate players, injured/suspended player, player not in club)",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Manager does not manage any club yet",
                    content = @Content)
    })
    @PutMapping("/lineup")
    public LineupResponse updateLineup(
            @AuthenticationPrincipal ManagerUserDetails principal,
            @Valid @RequestBody SetLineupRequest request
    ) {
        UUID clubId = resolveClubId(principal);
        return lineupService.updateLineup(clubId, request);
    }

    private UUID resolveClubId(ManagerUserDetails principal) {
        Manager manager = managerRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found: " + principal.getId()));
        if (manager.getClub() == null) {
            throw new InvlaidStateException("Manager does not manage any club yet");
        }
        return manager.getClub().getId();
    }
}
