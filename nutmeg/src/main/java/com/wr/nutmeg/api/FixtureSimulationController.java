package com.wr.nutmeg.api;

import com.wr.nutmeg.match.MatchSimulationService;
import com.wr.nutmeg.match.engine.MatchResult;
import com.wr.nutmeg.match.engine.SimulatedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dev/fixtures")
@Profile("dev")
public class FixtureSimulationController {

    private final MatchSimulationService matchSimulationService;

    public FixtureSimulationController(MatchSimulationService matchSimulationService) {
        this.matchSimulationService = matchSimulationService;
    }

    @PostMapping("/{fixtureId}/simulate")
    public SimulationResponse simulate(
            @PathVariable UUID fixtureId,
            @RequestParam(required = false) Long seed
    ) {
        MatchResult result = matchSimulationService.simulateFixture(fixtureId, seed);
        return SimulationResponse.from(result);
    }

    public record SimulationResponse(
            int homeScore,
            int awayScore,
            int homePossessions,
            int awayPossessions,
            List<EventResponse> events
    ) {
        static SimulationResponse from(MatchResult result) {
            return new SimulationResponse(
                    result.homeScore(),
                    result.awayScore(),
                    result.homePossessions(),
                    result.awayPossessions(),
                    result.events().stream().map(EventResponse::from).toList()
            );
        }
    }

    public record EventResponse(
            int minute,
            String type,
            String team,
            String player,
            String detail
    ) {
        static EventResponse from(SimulatedEvent event) {
            return new EventResponse(
                    event.minute(),
                    event.type().name(),
                    event.team().name(),
                    event.playerName(),
                    event.detail()
            );
        }
    }
}
