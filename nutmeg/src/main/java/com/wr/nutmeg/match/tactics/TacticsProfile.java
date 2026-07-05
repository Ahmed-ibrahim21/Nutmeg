package com.wr.nutmeg.match.tactics;

public record TacticsProfile(
        double attackBias,
        double defenseBias,
        double possessionBias,
        double pressIntensity,
        double widthBias,
        double tempoMultiplier,
        double cardRisk,
        double coherenceScore
) {
    public static TacticsProfile neutral() {
        return new TacticsProfile(0, 0, 0, 0, 0, 1, 0, 1);
    }
}
