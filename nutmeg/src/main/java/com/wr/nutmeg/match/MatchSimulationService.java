package com.wr.nutmeg.match;

import com.wr.nutmeg.club.Club;
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
import com.wr.nutmeg.match.setup.MatchTeamSetup;
import com.wr.nutmeg.match.setup.MatchTeamSetupRepository;
import com.wr.nutmeg.match.tactics.Formation;
import com.wr.nutmeg.match.tactics.FormationMatchupService;
import com.wr.nutmeg.match.tactics.MatchupModifiers;
import com.wr.nutmeg.match.tactics.TacticsCoherenceValidator;
import com.wr.nutmeg.match.tactics.TacticsProfile;
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
    private final MatchTeamSetupRepository matchTeamSetupRepository;
    private final MatchSetupService matchSetupService;
    private final PlayerRepository playerRepository;
    private final TacticsCoherenceValidator tacticsCoherenceValidator;
    private final FormationMatchupService formationMatchupService;
    private final MatchSimulator matchSimulator;

    public MatchSimulationService(
            FixtureRepository fixtureRepository,
            MatchTeamSetupRepository matchTeamSetupRepository,
            MatchSetupService matchSetupService,
            PlayerRepository playerRepository,
            TacticsCoherenceValidator tacticsCoherenceValidator,
            FormationMatchupService formationMatchupService,
            MatchSimulator matchSimulator
    ) {
        this.fixtureRepository = fixtureRepository;
        this.matchTeamSetupRepository = matchTeamSetupRepository;
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

        MatchTeamSetup homeSetup = ensureSetup(fixture, fixture.getHomeClub(), true, Formation.F_4_3_3);
        MatchTeamSetup awaySetup = ensureSetup(fixture, fixture.getAwayClub(), false, Formation.F_5_3_2);

        long seed = seedOverride != null
                ? seedOverride
                : fixture.getMatchSeed() != null
                ? fixture.getMatchSeed()
                : ThreadLocalRandom.current().nextLong();

        TeamState home = toTeamState(homeSetup, awaySetup.getTactics().getFormation());
        TeamState away = toTeamState(awaySetup, homeSetup.getTactics().getFormation());

        MatchResult result = matchSimulator.simulate(home, away, seed);

        persistResult(fixture, result, seed);
        return result;
    }

    private MatchTeamSetup ensureSetup(Fixture fixture, Club club, boolean homeTeam, Formation defaultFormation) {
        return matchTeamSetupRepository.findByFixtureIdAndClubId(fixture.getId(), club.getId())
                .orElseGet(() -> {
                    MatchTeamSetup setup = matchSetupService.buildDefaultSetup(fixture, club, homeTeam, defaultFormation);
                    return matchTeamSetupRepository.save(setup);
                });
    }

    private TeamState toTeamState(MatchTeamSetup setup, Formation opponentFormation) {
        Map<UUID, PlayerState> playersById = new HashMap<>();
        List<PlayerState> lineup = new ArrayList<>();

        for (LineupAssignment assignment : setup.getLineup()) {
            Player player = playerRepository.findById(assignment.getPlayerId())
                    .orElseThrow(() -> new IllegalStateException("Player not found: " + assignment.getPlayerId()));
            PlayerState state = PlayerState.from(player);
            playersById.put(state.id(), state);
            lineup.add(state);
        }

        TacticsProfile profile = tacticsCoherenceValidator.buildProfile(setup.getTactics());
        MatchupModifiers matchup = formationMatchupService.modifiersFor(
                setup.getTactics().getFormation(),
                opponentFormation
        );

        return new TeamState(setup.getClub(), setup.isHomeTeam(), profile, matchup, playersById, lineup);
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
