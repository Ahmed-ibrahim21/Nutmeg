package com.wr.nutmeg.fixture;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.common.enums.FixtureStatus;
import com.wr.nutmeg.league.League;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class FixtureSchedulerService {

    public List<Fixture> buildRoundRobinFixtures(League league, List<Club> clubs) {
        if (clubs.size() < 2) {
            throw new IllegalArgumentException("At least two clubs required to schedule fixtures");
        }

        List<Club> rotating = new ArrayList<>(clubs);
        if (rotating.size() % 2 != 0) {
            rotating.add(null);
        }

        int teamCount = rotating.size();
        int rounds = teamCount - 1;
        List<Fixture> fixtures = new ArrayList<>();

        for (int round = 0; round < rounds; round++) {
            fixtures.addAll(pairingsForRound(league, round + 1, rotating));
            rotate(rotating);
        }

        for (int round = 0; round < rounds; round++) {
            fixtures.addAll(reversePairingsForRound(league, rounds + round + 1, rotating));
            rotate(rotating);
        }

        return fixtures;
    }

    private List<Fixture> pairingsForRound(League league, int round, List<Club> clubs) {
        List<Fixture> fixtures = new ArrayList<>();
        for (int i = 0; i < clubs.size() / 2; i++) {
            Club home = clubs.get(i);
            Club away = clubs.get(clubs.size() - 1 - i);
            if (home != null && away != null) {
                fixtures.add(createFixture(league, round, home, away));
            }
        }
        return fixtures;
    }

    private List<Fixture> reversePairingsForRound(League league, int round, List<Club> clubs) {
        List<Fixture> fixtures = new ArrayList<>();
        for (int i = 0; i < clubs.size() / 2; i++) {
            Club first = clubs.get(i);
            Club second = clubs.get(clubs.size() - 1 - i);
            if (first != null && second != null) {
                fixtures.add(createFixture(league, round, second, first));
            }
        }
        return fixtures;
    }

    private Fixture createFixture(League league, int round, Club home, Club away) {
        Fixture fixture = new Fixture();
        fixture.setLeague(league);
        fixture.setRound(round);
        fixture.setHomeClub(home);
        fixture.setAwayClub(away);
        fixture.setStatus(FixtureStatus.SCHEDULED);
        fixture.setScheduledAt(Instant.now().plusSeconds(round * 86_400L));
        return fixture;
    }

    private void rotate(List<Club> clubs) {
        if (clubs.size() <= 2) {
            return;
        }
        Club last = clubs.remove(clubs.size() - 1);
        clubs.add(1, last);
    }
}
