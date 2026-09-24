package com.wr.nutmeg.league;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeagueStandingRepository extends JpaRepository<LeagueStanding, UUID> {

    Optional<LeagueStanding> findByLeagueIdAndClubId(UUID leagueId, UUID clubId);

    @Query("SELECT s FROM LeagueStanding s JOIN FETCH s.club WHERE s.league.id = :leagueId")
    List<LeagueStanding> findByLeagueIdWithClub(@Param("leagueId") UUID leagueId);
}
