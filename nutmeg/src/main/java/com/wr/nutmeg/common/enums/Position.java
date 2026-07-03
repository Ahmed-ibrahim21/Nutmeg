package com.wr.nutmeg.common.enums;

public enum Position {
    GK(PlayerRole.GK),
    CB(PlayerRole.DEF),
    RB(PlayerRole.DEF),
    LB(PlayerRole.DEF),
    CDM(PlayerRole.MID),
    CM(PlayerRole.MID),
    CAM(PlayerRole.MID),
    LM(PlayerRole.MID),
    RM(PlayerRole.MID),
    LW(PlayerRole.FWD),
    RW(PlayerRole.FWD),
    ST(PlayerRole.FWD);

    private final PlayerRole role;

    Position(PlayerRole role) {
        this.role = role;
    }

    public PlayerRole getRole() {
        return role;
    }
}
