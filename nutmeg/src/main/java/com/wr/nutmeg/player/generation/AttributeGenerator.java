package com.wr.nutmeg.player.generation;

import com.wr.nutmeg.common.enums.PlayerRole;
import com.wr.nutmeg.common.enums.Position;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class AttributeGenerator {

    private static final int MIN_ATTRIBUTE = 1;
    private static final int MAX_ATTRIBUTE = 99;

    public Map<String, Integer> generate(Position position, int tier) {
        int baseOvr = tierAnchor(tier) + randomOffset(-8, 8);
        baseOvr = clamp(baseOvr);

        Map<String, Integer> attributes = new HashMap<>();
        attributes.put("pace", rollAround(baseOvr));
        attributes.put("shooting", rollAround(baseOvr));
        attributes.put("passing", rollAround(baseOvr));
        attributes.put("dribbling", rollAround(baseOvr));
        attributes.put("defending", rollAround(baseOvr));
        attributes.put("physical", rollAround(baseOvr));
        attributes.put("stamina", rollAround(baseOvr));

        applyRoleBias(attributes, position.getRole());
        attributes.replaceAll((key, value) -> clamp(value));
        return attributes;
    }

    public int estimateOverall(Position position, Map<String, Integer> attributes) {
        PlayerRole role = position.getRole();
        return switch (role) {
            case GK -> average(attributes.get("defending"), attributes.get("physical"), attributes.get("stamina"));
            case DEF -> average(
                    attributes.get("defending"),
                    attributes.get("physical"),
                    attributes.get("pace"),
                    attributes.get("passing")
            );
            case MID -> average(
                    attributes.get("passing"),
                    attributes.get("dribbling"),
                    attributes.get("stamina"),
                    attributes.get("defending")
            );
            case FWD -> average(
                    attributes.get("shooting"),
                    attributes.get("pace"),
                    attributes.get("dribbling"),
                    attributes.get("passing")
            );
        };
    }

    private void applyRoleBias(Map<String, Integer> attributes, PlayerRole role) {
        switch (role) {
            case GK -> {
                boost(attributes, "defending", "physical");
                suppress(attributes, "shooting", "dribbling");
            }
            case DEF -> {
                boost(attributes, "defending", "physical", "pace");
                suppress(attributes, "shooting");
            }
            case MID -> {
                boost(attributes, "passing", "dribbling", "stamina");
                suppress(attributes, "defending");
            }
            case FWD -> {
                boost(attributes, "shooting", "pace", "dribbling");
                suppress(attributes, "defending");
            }
        }
    }

    private void boost(Map<String, Integer> attributes, String... keys) {
        for (String key : keys) {
            attributes.compute(key, (k, value) -> clamp(value + randomOffset(5, 15)));
        }
    }

    private void suppress(Map<String, Integer> attributes, String... keys) {
        for (String key : keys) {
            attributes.compute(key, (k, value) -> clamp(value + randomOffset(-8, -3)));
        }
    }

    private int tierAnchor(int tier) {
        return switch (tier) {
            case 1 -> 80;
            case 2 -> 70;
            case 3 -> 60;
            default -> Math.max(45, 90 - (tier * 10));
        };
    }

    private int rollAround(int base) {
        return clamp(base + randomOffset(-10, 10));
    }

    private int randomOffset(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private int clamp(int value) {
        return Math.max(MIN_ATTRIBUTE, Math.min(MAX_ATTRIBUTE, value));
    }

    private int average(int... values) {
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        return sum / values.length;
    }
}
