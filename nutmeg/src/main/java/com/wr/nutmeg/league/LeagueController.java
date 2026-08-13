package com.wr.nutmeg.league;

import com.wr.nutmeg.club.dtos.ClubResponse;
import com.wr.nutmeg.league.dtos.PagedLeagueResponse;
import com.wr.nutmeg.league.dtos.RoundSimulationResult;
import com.wr.nutmeg.league.dtos.SimulationResponse;
import com.wr.nutmeg.match.MatchSimulationService;
import com.wr.nutmeg.match.engine.MatchResult;
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
public class LeagueController {

    private final LeagueService leagueService;
    private final MatchSimulationService matchSimulationService;

    public LeagueController(LeagueService leagueService, MatchSimulationService matchSimulationService) {
        this.leagueService = leagueService;
        this.matchSimulationService = matchSimulationService;
    }

    @GetMapping
    public PagedLeagueResponse getPublicLeagues(@RequestParam(defaultValue = "0") int page) {
        Page<League> leagues = leagueService.getPublicLeagues(page);
        return PagedLeagueResponse.from(leagues);
    }

    @GetMapping("/{leagueId}/clubs")
    public List<ClubResponse> getClubs(@PathVariable UUID leagueId) {
        return leagueService.getClubsByLeagueId(leagueId).stream()
                .map(ClubResponse::from)
                .toList();
    }

    @PostMapping("/{leagueId}/simulate-round")
    public RoundSimulationResult simulateRound(@PathVariable UUID leagueId) {
        return leagueService.simulateCurrentRound(leagueId);
    }

     @PostMapping("/{fixtureId}/simulate")
    public SimulationResponse simulate(
            @PathVariable UUID fixtureId,
            @RequestParam(required = false) Long seed
    ) {
        MatchResult result = matchSimulationService.simulateFixture(fixtureId, seed);
        return SimulationResponse.from(result);
    }


}
