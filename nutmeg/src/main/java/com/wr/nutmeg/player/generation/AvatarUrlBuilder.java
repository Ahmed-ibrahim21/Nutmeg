package com.wr.nutmeg.player.generation;

import java.util.UUID;

public final class AvatarUrlBuilder {

    private static final String DICEBEAR_BASE = "https://api.dicebear.com/9.x/personas/svg?seed=";

    private AvatarUrlBuilder() {
    }

    public static String build(UUID seed) {
        return DICEBEAR_BASE + seed;
    }
}
