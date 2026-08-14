package com.wr.nutmeg.match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.wr.nutmeg.common.enums.FixtureStatus;

import java.util.List;
import java.util.UUID;
import com.wr.nutmeg.club.Club;


public interface FixtureRepository extends JpaRepository<Fixture, UUID> {

    List<Fixture> findByLeagueIdAndStatus(UUID leagueId, FixtureStatus status);

    List<Fixture> findByLeagueIdAndRoundAndStatus(UUID leagueId, int round, FixtureStatus status);

  @Query("""
    SELECT f
    FROM Fixture f
    WHERE f.homeClub = :club
       OR f.awayClub = :club
""")
List<Fixture> findByClub(@Param("club") Club club);
}
