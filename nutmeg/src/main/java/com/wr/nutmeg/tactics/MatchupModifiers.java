package com.wr.nutmeg.tactics;

public record MatchupModifiers(
        double attackBias,
        double defenseBias,
        double possessionBias,
        double widthBias,
        double counterVulnerability
) {
    public static MatchupModifiers none() {
        return new MatchupModifiers(0, 0, 0, 0, 0);
    }
}
