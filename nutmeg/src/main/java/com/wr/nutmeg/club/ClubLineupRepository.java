package com.wr.nutmeg.club;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubLineupRepository extends JpaRepository<ClubLineup,UUID> {

    Optional<ClubLineup> findByClubId(UUID clubId);
   
}
