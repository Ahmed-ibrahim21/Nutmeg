package com.wr.nutmeg.model;

import java.util.List;
import java.util.UUID;

public class League {
    private UUID leagueId;
    private String leagueName;
    private List<Club> clubs;
    private int totalRounds = clubs.size() * 2 - 2;

}
