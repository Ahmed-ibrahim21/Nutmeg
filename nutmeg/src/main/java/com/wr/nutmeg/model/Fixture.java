package com.wr.nutmeg.model;

import com.wr.nutmeg.enums.MatchEvents;

import java.util.Map;
import java.util.UUID;

public class Fixture {
    private UUID fixtureId;
    private Club firstClub;
    private Club secondClub;
    private int firstClubScore;
    private int secondClubScore;
    private Map<MatchEvents , Map<Integer,String>> Events;
}
