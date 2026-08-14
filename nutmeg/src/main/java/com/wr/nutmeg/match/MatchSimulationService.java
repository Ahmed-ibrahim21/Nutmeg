package com.wr.nutmeg.match;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubLineup;
import com.wr.nutmeg.common.enums.FixtureStatus;
import com.wr.nutmeg.common.enums.MatchEvents;
import com.wr.nutmeg.exceptions.InvalidArgumentsException;
import com.wr.nutmeg.exceptions.InvlaidStateException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MatchSimulationService {

    private static final int FITNESS_DROP_PER_MATCH = 15;
    private static final int MIN_FITNESS = 50;
    private static final int MORALE_WIN_BOOST = 5;
    private static final int MORALE_LOSS_DROP = 5;
    private static final int MAX_MORALE = 100;
    private static final int MIN_MORALE = 0;

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
                .orElseThrow(() -> new InvalidArgumentsException("Fixture not found: " + fixtureId));

        if (fixture.getStatus() == FixtureStatus.FINISHED) {
            throw new InvlaidStateException("Fixture already finished: " + fixtureId);
        }

        ClubLineup homeLineup = matchSetupService.getOrCreateLineup(fixture.getHomeClub(), Formation.F_4_4_2);
        ClubLineup awayLineup = matchSetupService.getOrCreateLineup(fixture.getAwayClub(), Formation.F_4_4_2);

        long seed = seedOverride != null
                ? seedOverride
                : fixture.getMatchSeed() != null
                ? fixture.getMatchSeed()
                : ThreadLocalRandom.current().nextLong();

        TeamState home = toTeamState(homeLineup, true, awayLineup.getTactics().getFormation());
        TeamState away = toTeamState(awayLineup, false, homeLineup.getTactics().getFormation());

        MatchResult result = matchSimulator.simulate(home, away, seed);

        persistResult(fixture, result, seed, homeLineup, awayLineup);
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

    private void persistResult(Fixture fixture, MatchResult result, long seed,
                               ClubLineup homeLineup, ClubLineup awayLineup) {
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

            updatePlayerStats(simulatedEvent);
        }

        updateAppearances(homeLineup, awayLineup);
        updateFitness(homeLineup, awayLineup);
        updateMorale(homeLineup, awayLineup, result.homeScore(), result.awayScore());

        fixtureRepository.save(fixture);
    }

    private void updatePlayerStats(SimulatedEvent event) {
        if (event.playerId() == null) {
            return;
        }
        switch (event.type()) {
            case GOAL -> playerRepository.findById(event.playerId()).ifPresent(player ->
                    player.setGoals(player.getGoals() + 1));
            case ASSIST -> playerRepository.findById(event.playerId()).ifPresent(player ->
                    player.setAssists(player.getAssists() + 1));
            case YELLOW_CARD -> playerRepository.findById(event.playerId()).ifPresent(player ->
                    player.setYellowCards(player.getYellowCards() + 1));
            case RED_CARD -> playerRepository.findById(event.playerId()).ifPresent(player -> {
                player.setRedCards(player.getRedCards() + 1);
                player.setSuspended(true);
            });
            default -> { /* no stat update for other event types */ }
        }
    }

    private void updateAppearances(ClubLineup homeLineup, ClubLineup awayLineup) {
        Set<UUID> participantIds = new HashSet<>();
        for (LineupAssignment assignment : homeLineup.getLineup()) {
            participantIds.add(assignment.getPlayerId());
        }
        for (LineupAssignment assignment : awayLineup.getLineup()) {
            participantIds.add(assignment.getPlayerId());
        }
        for (UUID playerId : participantIds) {
            playerRepository.findById(playerId).ifPresent(player ->
                    player.setAppearances(player.getAppearances() + 1));
        }
    }

    private void updateFitness(ClubLineup homeLineup, ClubLineup awayLineup) {
        List<LineupAssignment> allAssignments = new ArrayList<>(homeLineup.getLineup());
        allAssignments.addAll(awayLineup.getLineup());

        for (LineupAssignment assignment : allAssignments) {
            playerRepository.findById(assignment.getPlayerId()).ifPresent(player -> {
                int newFitness = Math.max(MIN_FITNESS, player.getCurrentFitness() - FITNESS_DROP_PER_MATCH);
                player.setCurrentFitness(newFitness);
            });
        }
    }

    private void updateMorale(ClubLineup homeLineup, ClubLineup awayLineup,
                              int homeScore, int awayScore) {
        int homeMoraleChange = homeScore > awayScore ? MORALE_WIN_BOOST
                : homeScore < awayScore ? -MORALE_LOSS_DROP : 0;
        int awayMoraleChange = -homeMoraleChange;

        applyMoraleChange(homeLineup, homeMoraleChange);
        applyMoraleChange(awayLineup, awayMoraleChange);
    }

    private void applyMoraleChange(ClubLineup lineup, int change) {
        if (change == 0) {
            return;
        }
        for (LineupAssignment assignment : lineup.getLineup()) {
            playerRepository.findById(assignment.getPlayerId()).ifPresent(player -> {
                int newMorale = Math.max(MIN_MORALE, Math.min(MAX_MORALE, player.getMorale() + change));
                player.setMorale(newMorale);
            });
        }
    }
}