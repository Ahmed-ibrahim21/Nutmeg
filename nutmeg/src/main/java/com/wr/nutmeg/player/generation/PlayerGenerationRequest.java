package com.wr.nutmeg.player.generation;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.common.enums.Position;
import com.wr.nutmeg.common.enums.PreferredFoot;

public record PlayerGenerationRequest(
        Position position,
        int tier,
        Club club,
        Integer jerseyNumber,
        Integer age
) {
}
