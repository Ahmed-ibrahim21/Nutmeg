package com.wr.nutmeg.fixture;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FixtureRepository extends JpaRepository<Fixture, UUID> {

    List<Fixture> findByLeagueIdAndStatus(UUID leagueId, com.wr.nutmeg.common.enums.FixtureStatus status);
}
