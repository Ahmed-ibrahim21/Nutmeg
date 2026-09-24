package com.wr.nutmeg.match;

import com.wr.nutmeg.league.LeagueStandingRepository;
import com.wr.nutmeg.match.engine.MatchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "nutmeg.seed.enabled=true",
        "spring.profiles.active=dev"
})
class MatchSimulationServiceTest {

    @Autowired
    private FixtureRepository fixtureRepository;

    @Autowired
    private MatchSimulationService matchSimulationService;

    @Autowired
    private LeagueStandingRepository leagueStandingRepository;

    @Test
    @Transactional
    void simulatesSeededFixtureEndToEnd() {
        var fixture = fixtureRepository.findAll().getFirst();

        MatchResult result = matchSimulationService.simulateFixture(fixture.getId(), 999L);

        assertThat(result.homeScore()).isGreaterThanOrEqualTo(0);
        assertThat(result.awayScore()).isGreaterThanOrEqualTo(0);
        assertThat(result.events()).isNotEmpty();

        var updated = fixtureRepository.findById(fixture.getId()).orElseThrow();
        assertThat(updated.getHomeScore()).isEqualTo(result.homeScore());
        assertThat(updated.getAwayScore()).isEqualTo(result.awayScore());

        var homeStanding = leagueStandingRepository
                .findByLeagueIdAndClubId(updated.getLeague().getId(), updated.getHomeClub().getId())
                .orElseThrow();
        var awayStanding = leagueStandingRepository
                .findByLeagueIdAndClubId(updated.getLeague().getId(), updated.getAwayClub().getId())
                .orElseThrow();

        assertThat(homeStanding.getPlayed()).isEqualTo(1);
        assertThat(awayStanding.getPlayed()).isEqualTo(1);
        assertThat(homeStanding.getGoalsFor()).isEqualTo(result.homeScore());
        assertThat(awayStanding.getGoalsFor()).isEqualTo(result.awayScore());
        if (result.homeScore() > result.awayScore()) {
            assertThat(homeStanding.getPoints()).isEqualTo(3);
            assertThat(awayStanding.getPoints()).isEqualTo(0);
        } else if (result.homeScore() < result.awayScore()) {
            assertThat(homeStanding.getPoints()).isEqualTo(0);
            assertThat(awayStanding.getPoints()).isEqualTo(3);
        } else {
            assertThat(homeStanding.getPoints()).isEqualTo(1);
            assertThat(awayStanding.getPoints()).isEqualTo(1);
        }
    }
}
