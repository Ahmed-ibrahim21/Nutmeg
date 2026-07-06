package com.wr.nutmeg.club;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.match.setup.LineupAssignment;
import com.wr.nutmeg.match.tactics.MatchTactics;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Embedded;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "club_lineups")
@Getter
@Setter
@NoArgsConstructor
public class ClubLineup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false, unique = true)
    private Club club;

    @Embedded
    private MatchTactics tactics = new MatchTactics();

    @ElementCollection
    @CollectionTable(name = "club_lineup_players", joinColumns = @JoinColumn(name = "lineup_id"))
    private List<LineupAssignment> lineup = new ArrayList<>();
}
