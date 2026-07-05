package com.wr.nutmeg.match.setup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchTeamSetupRepository extends JpaRepository<MatchTeamSetup, UUID> {

    List<MatchTeamSetup> findByFixtureId(UUID fixtureId);

    Optional<MatchTeamSetup> findByFixtureIdAndClubId(UUID fixtureId, UUID clubId);
}
