package com.wr.nutmeg.config;

import com.wr.nutmeg.club.ClubRepository;
import com.wr.nutmeg.league.LeagueRepository;
import com.wr.nutmeg.player.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "nutmeg.seed.enabled=true",
        "nutmeg.seed.club-count=8"
})
class GameWorldSeederTest {

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void seedsLeagueClubsAndPlayersOnStartup() {
        assertThat(leagueRepository.count()).isEqualTo(1);
        assertThat(clubRepository.count()).isEqualTo(8);
        assertThat(playerRepository.count()).isEqualTo(160);

        assertThat(playerRepository.findAll())
                .allMatch(player -> player.getClub() != null)
                .allMatch(player -> player.getAvatarUrl() != null && !player.getAvatarUrl().isBlank());
    }
}
