package com.wr.nutmeg.league;

import com.wr.nutmeg.club.Club;

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

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
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
    public LeagueService.RoundSimulationResult simulateRound(@PathVariable UUID leagueId) {
        return leagueService.simulateCurrentRound(leagueId);
    }

    // ---- Response DTOs ----

    public record LeagueResponse(
            UUID id,
            String name,
            String country,
            int tier,
            int currentRound,
            int totalRounds,
            int clubsNumber,
            String status
    ) {
        static LeagueResponse from(League league) {
            return new LeagueResponse(
                    league.getId(),
                    league.getName(),
                    league.getCountry(),
                    league.getTier(),
                    league.getCurrentRound(),
                    league.getTotalRounds(),
                    league.getClubsNumber(),
                    league.getStatus().name()
            );
        }
    }

    public record PagedLeagueResponse(
            List<LeagueResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        static PagedLeagueResponse from(Page<League> leaguePage) {
            return new PagedLeagueResponse(
                    leaguePage.getContent().stream().map(LeagueResponse::from).toList(),
                    leaguePage.getNumber(),
                    leaguePage.getSize(),
                    leaguePage.getTotalElements(),
                    leaguePage.getTotalPages()
            );
        }
    }

    public record ClubResponse(
            UUID id,
            String name,
            String shortName,
            String logoUrl,
            String stadiumName,
            long budget
    ) {
        static ClubResponse from(Club club) {
            return new ClubResponse(
                    club.getId(),
                    club.getName(),
                    club.getShortName(),
                    club.getLogoUrl(),
                    club.getStadiumName(),
                    club.getBudget()
            );
        }
    }
}
