package com.wr.nutmeg.league;

import com.wr.nutmeg.club.dtos.ClubResponse;
import com.wr.nutmeg.league.dtos.PagedLeagueResponse;
import com.wr.nutmeg.league.dtos.RoundSimulationResult;
import com.wr.nutmeg.league.dtos.SimulationResponse;
import com.wr.nutmeg.match.MatchSimulationService;
import com.wr.nutmeg.match.engine.MatchResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leagues")
@Tag(name = "League", description = "Browse leagues, view clubs, and simulate matches")
public class LeagueController {

    private final LeagueService leagueService;
    private final MatchSimulationService matchSimulationService;

    public LeagueController(LeagueService leagueService, MatchSimulationService matchSimulationService) {
        this.leagueService = leagueService;
        this.matchSimulationService = matchSimulationService;
    }

    @Operation(
            summary = "List public leagues",
            description = "Returns a paginated list of all public leagues. No authentication required.",
            security = @SecurityRequirement(name = "")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leagues returned",
                    content = @Content(schema = @Schema(implementation = PagedLeagueResponse.class)))
    })
    @GetMapping
    public PagedLeagueResponse getPublicLeagues(
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<League> leagues = leagueService.getPublicLeagues(page);
        return PagedLeagueResponse.from(leagues);
    }

    @Operation(
            summary = "List clubs in a league",
            description = "Returns all clubs belonging to the specified league."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clubs returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClubResponse.class)))),
            @ApiResponse(responseCode = "404", description = "League not found",
                    content = @Content)
    })
    @GetMapping("/{leagueId}/clubs")
    public List<ClubResponse> getClubs(
            @Parameter(description = "League ID", required = true)
            @PathVariable UUID leagueId
    ) {
        return leagueService.getClubsByLeagueId(leagueId).stream()
                .map(ClubResponse::from)
                .toList();
    }

    @Operation(
            summary = "Simulate the current round",
            description = "Simulates all fixtures in the current unplayed round of the league."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Round simulated",
                    content = @Content(schema = @Schema(implementation = RoundSimulationResult.class))),
            @ApiResponse(responseCode = "404", description = "League not found",
                    content = @Content)
    })
    @PostMapping("/{leagueId}/simulate-round")
    public RoundSimulationResult simulateRound(
            @Parameter(description = "League ID", required = true)
            @PathVariable UUID leagueId
    ) {
        return leagueService.simulateCurrentRound(leagueId);
    }

    @Operation(
            summary = "Simulate a single fixture",
            description = "Runs the match engine for a specific fixture. Optionally pass a seed for deterministic results."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Match simulated",
                    content = @Content(schema = @Schema(implementation = SimulationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Fixture not found",
                    content = @Content)
    })
    @PostMapping("/{fixtureId}/simulate")
    public SimulationResponse simulate(
            @Parameter(description = "Fixture ID", required = true)
            @PathVariable UUID fixtureId,
            @Parameter(description = "Optional RNG seed for deterministic simulation")
            @RequestParam(required = false) Long seed
    ) {
        MatchResult result = matchSimulationService.simulateFixture(fixtureId, seed);
        return SimulationResponse.from(result);
    }
}
