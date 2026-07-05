package com.wr.nutmeg.config;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.club.ClubRepository;
import com.wr.nutmeg.club.generation.ClubNameGenerator;
import com.wr.nutmeg.common.enums.LeagueStatus;
import com.wr.nutmeg.fixture.Fixture;
import com.wr.nutmeg.fixture.FixtureRepository;
import com.wr.nutmeg.fixture.FixtureSchedulerService;
import com.wr.nutmeg.league.League;
import com.wr.nutmeg.league.LeagueRepository;
import com.wr.nutmeg.player.PlayerRepository;
import com.wr.nutmeg.player.generation.PlayerGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "nutmeg.seed.enabled", havingValue = "true")
public class GameWorldSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GameWorldSeeder.class);

    private final NutmegSeedProperties seedProperties;
    private final LeagueRepository leagueRepository;
    private final ClubRepository clubRepository;
    private final PlayerRepository playerRepository;
    private final PlayerGenerationService playerGenerationService;
    private final ClubNameGenerator clubNameGenerator;
    private final FixtureRepository fixtureRepository;
    private final FixtureSchedulerService fixtureSchedulerService;

    public GameWorldSeeder(
            NutmegSeedProperties seedProperties,
            LeagueRepository leagueRepository,
            ClubRepository clubRepository,
            PlayerRepository playerRepository,
            PlayerGenerationService playerGenerationService,
            ClubNameGenerator clubNameGenerator,
            FixtureRepository fixtureRepository,
            FixtureSchedulerService fixtureSchedulerService
    ) {
        this.seedProperties = seedProperties;
        this.leagueRepository = leagueRepository;
        this.clubRepository = clubRepository;
        this.playerRepository = playerRepository;
        this.playerGenerationService = playerGenerationService;
        this.clubNameGenerator = clubNameGenerator;
        this.fixtureRepository = fixtureRepository;
        this.fixtureSchedulerService = fixtureSchedulerService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (leagueRepository.count() > 0) {
            log.info("Database already seeded, skipping.");
            return;
        }

        League league = createLeague();
        league = leagueRepository.save(league);

        Set<String> usedClubNames = new HashSet<>();
        List<Club> clubs = new ArrayList<>();
        for (int i = 0; i < seedProperties.getClubCount(); i++) {
            Club club = createClub(league, usedClubNames);
            club = clubRepository.save(club);
            playerRepository.saveAll(playerGenerationService.generateSquad(league.getTier(), club));
            clubs.add(club);
        }

        List<Fixture> fixtures = fixtureSchedulerService.buildRoundRobinFixtures(league, clubs);
        fixtureRepository.saveAll(fixtures);

        log.info(
                "Seeded league '{}' with {} clubs, {} players, and {} fixtures.",
                league.getName(),
                seedProperties.getClubCount(),
                playerRepository.count(),
                fixtures.size()
        );
    }

    private League createLeague() {
        League league = new League();
        league.setName(seedProperties.getLeagueName());
        league.setCountry(seedProperties.getCountry());
        league.setTier(seedProperties.getTier());
        league.setCurrentRound(0);
        league.setTotalRounds((seedProperties.getClubCount() - 1) * 2);
        league.setStatus(LeagueStatus.UPCOMING);
        return league;
    }

    private Club createClub(League league, Set<String> usedClubNames) {
        Club club = new Club();
        club.setName(uniqueClubName(usedClubNames));
        club.setShortName(createShortName(club.getName()));
        club.setStadiumName(club.getName() + " Arena");
        club.setStadiumCapacity(12_000 + league.getTier() * 2_000);
        club.setBudget(1_000_000L * league.getTier());
        club.setLogoUrl(buildClubLogoUrl());
        club.setLeague(league);
        return club;
    }

    private String uniqueClubName(Set<String> usedClubNames) {
        for (int attempt = 0; attempt < 50; attempt++) {
            String name = clubNameGenerator.generate();
            if (usedClubNames.add(name)) {
                return name;
            }
        }
        throw new IllegalStateException("Unable to generate unique club name");
    }

    private String createShortName(String clubName) {
        String[] parts = clubName.split(" ");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(3, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    private String buildClubLogoUrl() {
        return "https://api.dicebear.com/9.x/shapes/svg?seed=" + UUID.randomUUID();
    }
}
