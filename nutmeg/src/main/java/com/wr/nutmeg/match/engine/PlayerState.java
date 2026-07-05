package com.wr.nutmeg.match.engine;

import com.wr.nutmeg.common.enums.PlayerRole;
import com.wr.nutmeg.player.Player;

import java.util.UUID;

public record PlayerState(
        UUID id,
        String name,
        PlayerRole role,
        int overall,
        int passing,
        int shooting,
        int dribbling,
        int defending,
        int physical,
        int pace,
        int fitness
) {
    public static PlayerState from(Player player) {
        int fitness = player.getCurrentFitness() > 0 ? player.getCurrentFitness() : 100;
        return new PlayerState(
                player.getId(),
                player.getFullName(),
                player.getPosition().getRole(),
                player.getOverallRating(),
                player.getPassing(),
                player.getShooting(),
                player.getDribbling(),
                player.getDefending(),
                player.getPhysical(),
                player.getPace(),
                fitness
        );
    }
}
