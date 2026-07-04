package com.wr.nutmeg.player.generation;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.common.enums.Position;
import com.wr.nutmeg.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerGenerationServiceTest {

    private PlayerGenerationService playerGenerationService;

    @BeforeEach
    void setUp() {
        playerGenerationService = new PlayerGenerationService(new AttributeGenerator(), new NameGenerator());
    }

    @Test
    void generateCreatesPlayerWithAvatarAndContract() {
        Club club = new Club();
        club.setName("Test FC");

        Player player = playerGenerationService.generate(
                new PlayerGenerationRequest(Position.ST, 3, club, 9, 24)
        );

        assertThat(player.getAvatarUrl()).contains("api.dicebear.com");
        assertThat(player.getClub()).isEqualTo(club);
        assertThat(player.getJerseyNumber()).isEqualTo(9);
        assertThat(player.getAge()).isEqualTo(24);
        assertThat(player.getPotential()).isGreaterThanOrEqualTo(player.getOverallRating());
        assertThat(player.getMarketValue()).isPositive();
        assertThat(player.getWeeklyWage()).isPositive();
        assertThat(player.getContractExpiry()).isAfter(java.time.LocalDate.now());
    }

    @Test
    void generateSquadCreatesTwentyPlayersWithUniqueNamesAndNumbers() {
        Club club = new Club();
        club.setName("Test FC");

        List<Player> squad = playerGenerationService.generateSquad(3, club);

        assertThat(squad).hasSize(20);
        assertThat(squad).allMatch(player -> player.getClub() == club);

        Set<String> names = new HashSet<>();
        Set<Integer> numbers = new HashSet<>();
        for (Player player : squad) {
            assertThat(names.add(player.getFullName())).isTrue();
            assertThat(numbers.add(player.getJerseyNumber())).isTrue();
        }
    }

    @Test
    void generateProspectCreatesYoungFreeAgent() {
        Player prospect = playerGenerationService.generateProspect(3, Position.CAM);

        assertThat(prospect.getClub()).isNull();
        assertThat(prospect.getAge()).isBetween(16, 19);
        assertThat(prospect.getPosition()).isEqualTo(Position.CAM);
    }
}
