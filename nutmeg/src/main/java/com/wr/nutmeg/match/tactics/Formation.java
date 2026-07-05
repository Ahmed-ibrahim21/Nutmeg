package com.wr.nutmeg.match.tactics;

public enum Formation {
    F_4_4_2(FormationArchetype.BALANCED, 4, 4, 2),
    F_4_3_3(FormationArchetype.ATTACKING, 4, 3, 3),
    F_4_2_3_1(FormationArchetype.BALANCED, 4, 5, 1),
    F_5_3_2(FormationArchetype.DEFENSIVE, 5, 3, 2),
    F_4_5_1(FormationArchetype.DEFENSIVE, 4, 5, 1),
    F_3_4_3(FormationArchetype.ATTACKING, 3, 4, 3);

    private final FormationArchetype archetype;
    private final int defenders;
    private final int midfielders;
    private final int forwards;

    Formation(FormationArchetype archetype, int defenders, int midfielders, int forwards) {
        this.archetype = archetype;
        this.defenders = defenders;
        this.midfielders = midfielders;
        this.forwards = forwards;
    }

    public FormationArchetype getArchetype() {
        return archetype;
    }

    public int getDefenders() {
        return defenders;
    }

    public int getMidfielders() {
        return midfielders;
    }

    public int getForwards() {
        return forwards;
    }
}
