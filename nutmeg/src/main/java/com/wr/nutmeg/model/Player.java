package com.wr.nutmeg.model;

//id, clubId (nullable if free agent),
// name, age, position (enum: GK/DEF/MID/FWD), pace, passing, shooting,
// defending, stamina, currentFitness (0-100),
// morale, marketValue, contractExpiry,
// potential (hidden, used for future dev)

import com.wr.nutmeg.enums.Position;

import java.util.UUID;

public class Player {

   private UUID id;
    private String firstName;
    private  String lastName;
    private int age;
    private  UUID clubId;
    private Position position;
    private  String imageUrl;
    private int stamina ;
}
