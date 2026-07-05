package com.wr.nutmeg.match.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class MatchContext {

    private final TeamState home;
    private final TeamState away;
    private final Random random;
    private final List<SimulatedEvent> events = new ArrayList<>();

    private int minute;
    private TeamSide possession;
    private PitchZone zone;
    private PlayerState ballCarrier;
    private int homeScore;
    private int awayScore;
    private int homePossessions;
    private int awayPossessions;

    public MatchContext(TeamState home, TeamState away, long seed) {
        this.home = home;
        this.away = away;
        this.random = new Random(seed);
        this.minute = 1;
        this.possession = TeamSide.HOME;
        this.zone = PitchZone.MIDFIELD;
        this.ballCarrier = pickCarrier(teamInPossession(), PitchZone.MIDFIELD);
    }

    public TeamState home() {
        return home;
    }

    public TeamState away() {
        return away;
    }

    public Random random() {
        return random;
    }

    public List<SimulatedEvent> events() {
        return events;
    }

    public int minute() {
        return minute;
    }

    public TeamSide possession() {
        return possession;
    }

    public PitchZone zone() {
        return zone;
    }

    public PlayerState ballCarrier() {
        return ballCarrier;
    }

    public int homeScore() {
        return homeScore;
    }

    public int awayScore() {
        return awayScore;
    }

    public int homePossessions() {
        return homePossessions;
    }

    public int awayPossessions() {
        return awayPossessions;
    }

    public TeamState teamInPossession() {
        return possession == TeamSide.HOME ? home : away;
    }

    public TeamState defendingTeam() {
        return possession == TeamSide.HOME ? away : home;
    }

    public void setPossession(TeamSide possession) {
        this.possession = possession;
    }

    public void setZone(PitchZone zone) {
        this.zone = zone;
    }

    public void setBallCarrier(PlayerState ballCarrier) {
        this.ballCarrier = ballCarrier;
    }

    public void advanceMinute(int amount) {
        minute = Math.min(90, minute + amount);
    }

    public void recordPossessionStart() {
        if (possession == TeamSide.HOME) {
            homePossessions++;
        } else {
            awayPossessions++;
        }
    }

    public void addEvent(SimulatedEvent event) {
        events.add(event);
    }

    public void score(TeamSide team) {
        if (team == TeamSide.HOME) {
            homeScore++;
        } else {
            awayScore++;
        }
    }

    public PlayerState pickCarrier(TeamState team, PitchZone targetZone) {
        List<PlayerState> candidates = team.lineup().stream()
                .filter(player -> player.role() != com.wr.nutmeg.common.enums.PlayerRole.GK)
                .toList();
        if (candidates.isEmpty()) {
            return team.lineup().getFirst();
        }

        com.wr.nutmeg.common.enums.PlayerRole preferredRole = switch (targetZone) {
            case DEFENSIVE_THIRD -> com.wr.nutmeg.common.enums.PlayerRole.DEF;
            case MIDFIELD -> com.wr.nutmeg.common.enums.PlayerRole.MID;
            case ATTACKING_THIRD -> com.wr.nutmeg.common.enums.PlayerRole.FWD;
        };

        return candidates.stream()
                .filter(player -> player.role() == preferredRole)
                .max(java.util.Comparator.comparingInt(PlayerState::overall))
                .orElse(candidates.get(random.nextInt(candidates.size())));
    }

    public UUID clubId(TeamSide teamSide) {
        return teamSide == TeamSide.HOME ? home.club().getId() : away.club().getId();
    }
}
