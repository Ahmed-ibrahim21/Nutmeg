package com.wr.nutmeg.match.tactics;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class MatchTactics {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Formation formation = Formation.F_4_4_2;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GamePlan gamePlan = GamePlan.PASSING_GAME;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ForwardLine forwardLine = ForwardLine.SUPPORT_MIDFIELD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MidfieldLine midfieldLine = MidfieldLine.STAY_IN_POSITION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DefenseLine defenseLine = DefenseLine.DEFEND_DEEP;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Pressing pressing = Pressing.BALANCED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Style style = Style.BALANCED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tempo tempo = Tempo.POSSESSION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tackling tackling = Tackling.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Marking marking = Marking.ZONAL;

    @Column(nullable = false)
    private boolean offsideTrap = false;
}
