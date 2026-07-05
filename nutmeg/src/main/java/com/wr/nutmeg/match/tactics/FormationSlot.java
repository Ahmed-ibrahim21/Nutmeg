package com.wr.nutmeg.match.tactics;

import com.wr.nutmeg.common.enums.PlayerRole;

public enum FormationSlot {
    GK(PlayerRole.GK),
    LB(PlayerRole.DEF),
    CB1(PlayerRole.DEF),
    CB2(PlayerRole.DEF),
    CB3(PlayerRole.DEF),
    RB(PlayerRole.DEF),
    LWB(PlayerRole.DEF),
    RWB(PlayerRole.DEF),
    LM(PlayerRole.MID),
    CM1(PlayerRole.MID),
    CM2(PlayerRole.MID),
    CM3(PlayerRole.MID),
    CDM(PlayerRole.MID),
    CAM(PlayerRole.MID),
    RM(PlayerRole.MID),
    LW(PlayerRole.FWD),
    RW(PlayerRole.FWD),
    ST1(PlayerRole.FWD),
    ST2(PlayerRole.FWD),
    ST(PlayerRole.FWD);

    private final PlayerRole role;

    FormationSlot(PlayerRole role) {
        this.role = role;
    }

    public PlayerRole getRole() {
        return role;
    }
}
