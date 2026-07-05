package com.wr.nutmeg.match.setup;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.common.enums.PlayerRole;
import com.wr.nutmeg.fixture.Fixture;
import com.wr.nutmeg.match.tactics.DefaultTacticsFactory;
import com.wr.nutmeg.match.tactics.Formation;
import com.wr.nutmeg.match.tactics.FormationSlot;
import com.wr.nutmeg.player.Player;
import com.wr.nutmeg.player.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class MatchSetupService {

    private final MatchTeamSetupRepository matchTeamSetupRepository;
    private final PlayerRepository playerRepository;
    private final DefaultTacticsFactory defaultTacticsFactory;

    public MatchSetupService(
            MatchTeamSetupRepository matchTeamSetupRepository,
            PlayerRepository playerRepository,
            DefaultTacticsFactory defaultTacticsFactory
    ) {
        this.matchTeamSetupRepository = matchTeamSetupRepository;
        this.playerRepository = playerRepository;
        this.defaultTacticsFactory = defaultTacticsFactory;
    }

    @Transactional(readOnly = true)
    public MatchTeamSetup getOrCreateDefaultSetup(Fixture fixture, Club club, boolean homeTeam, Formation formation) {
        return matchTeamSetupRepository.findByFixtureIdAndClubId(fixture.getId(), club.getId())
                .orElseGet(() -> buildDefaultSetup(fixture, club, homeTeam, formation));
    }

    public MatchTeamSetup buildDefaultSetup(Fixture fixture, Club club, boolean homeTeam, Formation formation) {
        List<Player> squad = playerRepository.findAll().stream()
                .filter(player -> player.getClub() != null && player.getClub().getId().equals(club.getId()))
                .filter(player -> !player.isSuspended() && !player.isInjured())
                .toList();

        MatchTeamSetup setup = new MatchTeamSetup();
        setup.setFixture(fixture);
        setup.setClub(club);
        setup.setHomeTeam(homeTeam);
        setup.setTactics(defaultTacticsFactory.forFormation(formation));
        setup.setLineup(buildLineup(squad, formation));
        return setup;
    }

    private List<LineupAssignment> buildLineup(List<Player> squad, Formation formation) {
        List<FormationSlot> slots = FormationTemplate.slotsFor(formation);
        Map<PlayerRole, List<Player>> byRole = new EnumMap<>(PlayerRole.class);
        for (PlayerRole role : PlayerRole.values()) {
            byRole.put(role, new ArrayList<>());
        }
        for (Player player : squad) {
            byRole.get(player.getPosition().getRole()).add(player);
        }
        for (List<Player> players : byRole.values()) {
            players.sort(Comparator.comparingInt(Player::getOverallRating).reversed());
        }

        Set<UUID> used = new HashSet<>();
        List<LineupAssignment> assignments = new ArrayList<>();
        for (FormationSlot slot : slots) {
            Player selected = byRole.get(slot.getRole()).stream()
                    .filter(player -> !used.contains(player.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Not enough players for slot " + slot + " in formation " + formation
                    ));
            used.add(selected.getId());
            LineupAssignment assignment = new LineupAssignment();
            assignment.setSlot(slot);
            assignment.setPlayerId(selected.getId());
            assignments.add(assignment);
        }
        return assignments;
    }
}
