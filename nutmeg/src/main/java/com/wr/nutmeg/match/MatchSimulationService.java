package com.wr.nutmeg.match;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubLineup;
import com.wr.nutmeg.common.enums.FixtureStatus;
import com.wr.nutmeg.common.enums.MatchEvents;
import com.wr.nutmeg.fixture.Fixture;
import com.wr.nutmeg.fixture.FixtureRepository;
import com.wr.nutmeg.fixture.MatchEvent;
import com.wr.nutmeg.match.engine.MatchResult;
import com.wr.nutmeg.match.engine.MatchSimulator;
import com.wr.nutmeg.match.engine.PlayerState;
import com.wr.nutmeg.match.engine.SimulatedEvent;
import com.wr.nutmeg.match.engine.TeamState;
import com.wr.nutmeg.match.setup.LineupAssignment;
import com.wr.nutmeg.match.setup.MatchSetupService;
import com.wr.nutmeg.tactics.Formation;
import com.wr.nutmeg.tactics.FormationMatchupService;
import com.wr.nutmeg.tactics.MatchupModifiers;
import com.wr.nutmeg.tactics.TacticsCoherenceValidator;
import com.wr.nutmeg.tactics.TacticsProfile;
import com.wr.nutmeg.player.Player;
import com.wr.nutmeg.player.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MatchSimulationService {

    private final FixtureRepository fixtureRepository;
    private final MatchSetupService matchSetupService;
    private final PlayerRepository playerRepository;
    private final TacticsCoherenceValidator tacticsCoherenceValidator;
    private final FormationMatchupService formationMatchupService;
    private final MatchSimulator matchSimulator;

    public MatchSimulationService(
            FixtureRepository fixtureRepository,
            MatchSetupService matchSetupService,
            PlayerRepository playerRepository,
            TacticsCoherenceValidator tacticsCoherenceValidator,
            FormationMatchupService formationMatchupService,
            MatchSimulator matchSimulator
    ) {
        this.fixtureRepository = fixtureRepository;
        this.matchSetupService = matchSetupService;
        this.playerRepository = playerRepository;
        this.tacticsCoherenceValidator = tacticsCoherenceValidator;
        this.formationMatchupService = formationMatchupService;
        this.matchSimulator = matchSimulator;
    }

    @Transactional
    public MatchResult simulateFixture(UUID fixtureId, Long seedOverride) {
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("Fixture not found: " + fixtureId));

        if (fixture.getStatus() == FixtureStatus.FINISHED) {
            throw new IllegalStateException("Fixture already finished: " + fixtureId);
        }

        ClubLineup homeLineup = matchSetupService.getOrCreateLineup(fixture.getHomeClub(), Formation.F_4_3_3);
        ClubLineup awayLineup = matchSetupService.getOrCreateLineup(fixture.getAwayClub(), Formation.F_5_3_2);

        long seed = seedOverride != null
                ? seedOverride
                : fixture.getMatchSeed() != null
                ? fixture.getMatchSeed()
                : ThreadLocalRandom.current().nextLong();

        TeamState home = toTeamState(homeLineup, true, awayLineup.getTactics().getFormation());
        TeamState away = toTeamState(awayLineup, false, homeLineup.getTactics().getFormation());

        MatchResult result = matchSimulator.simulate(home, away, seed);

        persistResult(fixture, result, seed);
        return result;
    }

    private TeamState toTeamState(ClubLineup clubLineup, boolean homeTeam, Formation opponentFormation) {
        Map<UUID, PlayerState> playersById = new HashMap<>();
        List<PlayerState> lineup = new ArrayList<>();

        for (LineupAssignment assignment : clubLineup.getLineup()) {
            Player player = playerRepository.findById(assignment.getPlayerId())
                    .orElseThrow(() -> new IllegalStateException("Player not found: " + assignment.getPlayerId()));
            PlayerState state = PlayerState.from(player);
            playersById.put(state.id(), state);
            lineup.add(state);
        }

        TacticsProfile profile = tacticsCoherenceValidator.buildProfile(clubLineup.getTactics());
        MatchupModifiers matchup = formationMatchupService.modifiersFor(
                clubLineup.getTactics().getFormation(),
                opponentFormation
        );

        return new TeamState(clubLineup.getClub(), homeTeam, profile, matchup, playersById, lineup);
    }

    private void persistResult(Fixture fixture, MatchResult result, long seed) {
        fixture.getEvents().clear();
        fixture.setHomeScore(result.homeScore());
        fixture.setAwayScore(result.awayScore());
        fixture.setMatchSeed(seed);
        fixture.setStatus(FixtureStatus.FINISHED);

        for (SimulatedEvent simulatedEvent : result.events()) {
            MatchEvent event = new MatchEvent();
            event.setFixture(fixture);
            event.setMinute(simulatedEvent.minute());
            event.setType(simulatedEvent.type());
            event.setDetail(simulatedEvent.detail());

            Club club = simulatedEvent.team() == com.wr.nutmeg.match.engine.TeamSide.HOME
                    ? fixture.getHomeClub()
                    : fixture.getAwayClub();
            event.setClub(club);

            if (simulatedEvent.playerId() != null) {
                event.setPlayer(playerRepository.getReferenceById(simulatedEvent.playerId()));
            }
            if (simulatedEvent.relatedPlayerId() != null) {
                event.setRelatedPlayer(playerRepository.getReferenceById(simulatedEvent.relatedPlayerId()));
            }

            fixture.addEvent(event);

            if (simulatedEvent.type() == MatchEvents.GOAL && simulatedEvent.playerId() != null) {
                playerRepository.findById(simulatedEvent.playerId()).ifPresent(player -> {
                    player.setGoals(player.getGoals() + 1);
                    player.setAppearances(player.getAppearances() + 1);
                });
            }
        }

        fixtureRepository.save(fixture);
    }
}