package com.wr.nutmeg.match.setup;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.fixture.Fixture;
import com.wr.nutmeg.match.tactics.MatchTactics;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "match_team_setups",
        uniqueConstraints = @UniqueConstraint(columnNames = {"fixture_id", "club_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class MatchTeamSetup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fixture_id", nullable = false)
    private Fixture fixture;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(nullable = false)
    private boolean homeTeam;

    @Embedded
    private MatchTactics tactics = new MatchTactics();

    @ElementCollection
    @CollectionTable(name = "match_lineup", joinColumns = @JoinColumn(name = "setup_id"))
    private List<LineupAssignment> lineup = new ArrayList<>();
}
