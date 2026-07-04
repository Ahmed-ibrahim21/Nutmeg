package com.wr.nutmeg.player.generation;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class NameGenerator {

    private static final List<String> FIRST_NAMES = List.of(
            "Alex", "Ben", "Carlos", "Diego", "Ethan", "Finn", "Gabriel", "Hugo",
            "Ivan", "Jack", "Kai", "Leo", "Marco", "Noah", "Omar", "Pedro",
            "Quinn", "Ryan", "Sam", "Tom", "Victor", "Will", "Yusuf", "Zain",
            "Adam", "Bruno", "Cole", "Daniel", "Erik", "Felix", "George", "Henry"
    );

    private static final List<String> LAST_NAMES = List.of(
            "Silva", "Santos", "Costa", "Rivera", "Brooks", "Carter", "Davis", "Evans",
            "Foster", "Garcia", "Hall", "Ito", "Johnson", "Khan", "Lopez", "Miller",
            "Nguyen", "Okafor", "Patel", "Quinn", "Reed", "Stone", "Turner", "Vargas",
            "Walker", "Young", "Zimmerman", "Adams", "Baker", "Clark", "Edwards", "Green"
    );

    private static final List<String> NATIONALITIES = List.of(
            "England", "Spain", "Brazil", "France", "Germany", "Italy", "Portugal", "Netherlands"
    );

    public String randomFirstName() {
        return pick(FIRST_NAMES);
    }

    public String randomLastName() {
        return pick(LAST_NAMES);
    }

    public String randomNationality() {
        return pick(NATIONALITIES);
    }

    private String pick(List<String> values) {
        return values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }
}
