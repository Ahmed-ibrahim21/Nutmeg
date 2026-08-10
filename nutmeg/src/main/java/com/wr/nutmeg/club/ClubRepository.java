package com.wr.nutmeg.club;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClubRepository extends JpaRepository<Club, UUID> {

    List<Club> findByLeagueId(UUID leagueId);
}
