package com.wr.nutmeg.match.engine;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.common.enums.PlayerRole;
import com.wr.nutmeg.match.tactics.MatchupModifiers;
import com.wr.nutmeg.match.tactics.TacticsProfile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TeamState(
        Club club,
        boolean homeTeam,
        TacticsProfile tactics,
        MatchupModifiers matchup,
        Map<UUID, PlayerState> playersById,
        List<PlayerState> lineup
) {
    public double averageOverall() {
        return lineup.stream().mapToInt(PlayerState::overall).average().orElse(60);
    }

    public double lineOverall(PlayerRole... roles) {
        return lineup.stream()
                .filter(player -> containsRole(player.role(), roles))
                .mapToInt(PlayerState::overall)
                .average()
                .orElse(averageOverall());
    }

    public PlayerState goalkeeper() {
        return lineup.stream()
                .filter(player -> player.role() == PlayerRole.GK)
                .findFirst()
                .orElse(lineup.getFirst());
    }

    private boolean containsRole(PlayerRole role, PlayerRole... roles) {
        for (PlayerRole candidate : roles) {
            if (candidate == role) {
                return true;
            }
        }
        return false;
    }
}
