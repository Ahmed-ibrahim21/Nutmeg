package com.wr.nutmeg.fixture;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.common.enums.FixtureStatus;
import com.wr.nutmeg.league.League;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "fixtures")
@Getter
@Setter
@NoArgsConstructor
public class Fixture {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @Column(nullable = false)
    private int round;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_club_id", nullable = false)
    private Club homeClub;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "away_club_id", nullable = false)
    private Club awayClub;

    private int homeScore;

    private int awayScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FixtureStatus status = FixtureStatus.SCHEDULED;

    private Instant scheduledAt;

    private Long matchSeed;

    @OneToMany(mappedBy = "fixture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchEvent> events = new ArrayList<>();

    public void addEvent(MatchEvent event) {
        events.add(event);
        event.setFixture(this);
    }
}
