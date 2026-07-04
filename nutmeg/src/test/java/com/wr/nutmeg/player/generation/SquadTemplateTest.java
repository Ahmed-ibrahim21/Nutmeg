package com.wr.nutmeg.player.generation;

import com.wr.nutmeg.common.enums.Position;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SquadTemplateTest {

    @Test
    void squadTemplateProvidesTwentyPositions() {
        assertThat(SquadTemplate.positionsForSquad()).hasSize(20);
    }
}
