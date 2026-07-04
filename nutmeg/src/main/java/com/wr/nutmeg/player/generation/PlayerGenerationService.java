package com.wr.nutmeg.player.generation;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.common.enums.Position;
import com.wr.nutmeg.common.enums.PreferredFoot;
import com.wr.nutmeg.player.Player;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PlayerGenerationService {

    private static final int MAX_NAME_ATTEMPTS = 25;

    private final AttributeGenerator attributeGenerator;
    private final NameGenerator nameGenerator;

    public PlayerGenerationService(AttributeGenerator attributeGenerator, NameGenerator nameGenerator) {
        this.attributeGenerator = attributeGenerator;
        this.nameGenerator = nameGenerator;
    }

    public Player generate(PlayerGenerationRequest request) {
        Map<String, Integer> attributes = attributeGenerator.generate(request.position(), request.tier());
        int age = request.age() != null ? request.age() : randomAge();
        int overall = attributeGenerator.estimateOverall(request.position(), attributes);

        Player player = new Player();
        player.setFirstName(nameGenerator.randomFirstName());
        player.setLastName(nameGenerator.randomLastName());
        player.setNationality(nameGenerator.randomNationality());
        player.setAge(age);
        player.setPosition(request.position());
        player.setPreferredFoot(randomPreferredFoot());
        player.setClub(request.club());
        player.setAvatarUrl(AvatarUrlBuilder.build(UUID.randomUUID()));
        player.setPace(attributes.get("pace"));
        player.setShooting(attributes.get("shooting"));
        player.setPassing(attributes.get("passing"));
        player.setDribbling(attributes.get("dribbling"));
        player.setDefending(attributes.get("defending"));
        player.setPhysical(attributes.get("physical"));
        player.setStamina(attributes.get("stamina"));
        player.setCurrentFitness(100);
        player.setMorale(75);
        player.setPotential(calculatePotential(overall, age));
        player.setMarketValue(calculateMarketValue(overall, request.tier()));
        player.setWeeklyWage(calculateWeeklyWage(player.getMarketValue()));
        player.setContractExpiry(LocalDate.now().plusYears(randomContractYears()));
        player.setJerseyNumber(request.jerseyNumber());
        return player;
    }

    public List<Player> generateSquad(int tier, Club club) {
        List<Position> positions = SquadTemplate.positionsForSquad();
        List<Player> squad = new ArrayList<>(positions.size());
        Set<String> usedNames = new HashSet<>();
        Set<Integer> usedNumbers = new HashSet<>();

        for (Position position : positions) {
            Player player = generateUniquePlayer(tier, club, position, usedNames, usedNumbers);
            squad.add(player);
        }
        return squad;
    }

    public Player generateProspect(int tier, Position position) {
        int age = ThreadLocalRandom.current().nextInt(16, 20);
        return generate(new PlayerGenerationRequest(position, tier, null, null, age));
    }

    private Player generateUniquePlayer(
            int tier,
            Club club,
            Position position,
            Set<String> usedNames,
            Set<Integer> usedNumbers
    ) {
        for (int attempt = 0; attempt < MAX_NAME_ATTEMPTS; attempt++) {
            int jerseyNumber = nextJerseyNumber(usedNumbers);
            Player player = generate(new PlayerGenerationRequest(position, tier, club, jerseyNumber, null));
            String fullName = player.getFullName();
            if (usedNames.add(fullName)) {
                usedNumbers.add(jerseyNumber);
                return player;
            }
        }
        throw new IllegalStateException("Unable to generate unique player name for club " + club.getName());
    }

    private int nextJerseyNumber(Set<Integer> usedNumbers) {
        for (int number = 1; number <= 99; number++) {
            if (!usedNumbers.contains(number)) {
                return number;
            }
        }
        throw new IllegalStateException("No available jersey numbers left");
    }

    private int randomAge() {
        return ThreadLocalRandom.current().nextInt(18, 36);
    }

    private PreferredFoot randomPreferredFoot() {
        PreferredFoot[] values = PreferredFoot.values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    private int calculatePotential(int overall, int age) {
        int bonus = age <= 21
                ? ThreadLocalRandom.current().nextInt(8, 21)
                : ThreadLocalRandom.current().nextInt(5, 16);
        return Math.min(99, Math.max(overall, overall + bonus));
    }

    private long calculateMarketValue(int overall, int tier) {
        double tierMultiplier = switch (tier) {
            case 1 -> 1.5;
            case 2 -> 1.2;
            case 3 -> 1.0;
            default -> 0.8;
        };
        return Math.round(overall * overall * tierMultiplier * 1_000L);
    }

    private long calculateWeeklyWage(long marketValue) {
        return Math.max(500L, marketValue / 200L);
    }

    private int randomContractYears() {
        return ThreadLocalRandom.current().nextInt(1, 4);
    }
}
