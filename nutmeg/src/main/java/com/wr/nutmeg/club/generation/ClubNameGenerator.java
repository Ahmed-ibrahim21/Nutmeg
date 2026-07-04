package com.wr.nutmeg.club.generation;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ClubNameGenerator {

    private static final List<String> PREFIXES = List.of(
            "North", "South", "East", "West", "Riverside", "Oakwood", "Ironbridge", "Silverdale",
            "Redwood", "Bayview", "Highfield", "Stonegate"
    );

    private static final List<String> SUFFIXES = List.of(
            "United", "Athletic", "Town", "City", "Rovers", "Wanderers", "FC", "Albion"
    );

    public String generate() {
        String prefix = PREFIXES.get(ThreadLocalRandom.current().nextInt(PREFIXES.size()));
        String suffix = SUFFIXES.get(ThreadLocalRandom.current().nextInt(SUFFIXES.size()));
        return prefix + " " + suffix;
    }
}
