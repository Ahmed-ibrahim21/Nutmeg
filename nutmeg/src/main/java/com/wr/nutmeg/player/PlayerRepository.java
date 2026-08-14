package com.wr.nutmeg.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;
import com.wr.nutmeg.club.Club;


public interface PlayerRepository extends JpaRepository<Player, UUID> {
     List<Player> findByClub(Club club);
}
