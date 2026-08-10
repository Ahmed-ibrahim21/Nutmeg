package com.wr.nutmeg.league;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LeagueRepository extends JpaRepository<League, UUID> {

    Page<League> findByLeagueVisibility(LeagueVisibility visibility, Pageable pageable);
}
