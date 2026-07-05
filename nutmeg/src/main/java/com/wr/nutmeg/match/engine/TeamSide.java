package com.wr.nutmeg.match.engine;

public enum TeamSide {
    HOME,
    AWAY;

    public TeamSide opposite() {
        return this == HOME ? AWAY : HOME;
    }
}
