package com.wr.nutmeg.player.generation;

import com.wr.nutmeg.common.enums.Position;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeGeneratorTest {

    private final AttributeGenerator attributeGenerator = new AttributeGenerator();

    @Test
    void defendersAverageHigherDefendingThanForwardsAcrossManyRolls() {
        double defenderTotal = 0;
        double forwardTotal = 0;
        int iterations = 100;

        for (int i = 0; i < iterations; i++) {
            defenderTotal += attributeGenerator.generate(Position.CB, 3).get("defending");
            forwardTotal += attributeGenerator.generate(Position.ST, 3).get("defending");
        }

        assertThat(defenderTotal / iterations).isGreaterThan(forwardTotal / iterations);
    }

    @Test
    void attributesStayWithinValidRange() {
        Map<String, Integer> attributes = attributeGenerator.generate(Position.CM, 2);

        attributes.values().forEach(value -> assertThat(value).isBetween(1, 99));
    }

    @Test
    void estimateOverallUsesRoleSpecificAttributes() {
        Map<String, Integer> attributes = Map.of(
                "pace", 70,
                "shooting", 80,
                "passing", 75,
                "dribbling", 78,
                "defending", 40,
                "physical", 65,
                "stamina", 72
        );

        int forwardOverall = attributeGenerator.estimateOverall(Position.ST, attributes);
        assertThat(forwardOverall).isEqualTo(75);
    }
}
