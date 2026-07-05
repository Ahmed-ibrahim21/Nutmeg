package com.wr.nutmeg.match;

import com.wr.nutmeg.fixture.FixtureRepository;
import com.wr.nutmeg.match.engine.MatchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

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

    @Test
    void simulatesSeededFixtureEndToEnd() {
        var fixture = fixtureRepository.findAll().getFirst();

        MatchResult result = matchSimulationService.simulateFixture(fixture.getId(), 999L);

        assertThat(result.homeScore()).isGreaterThanOrEqualTo(0);
        assertThat(result.awayScore()).isGreaterThanOrEqualTo(0);
        assertThat(result.events()).isNotEmpty();

        var updated = fixtureRepository.findById(fixture.getId()).orElseThrow();
        assertThat(updated.getHomeScore()).isEqualTo(result.homeScore());
        assertThat(updated.getAwayScore()).isEqualTo(result.awayScore());
    }
}
