package com.wr.nutmeg.league;

import com.wr.nutmeg.club.Club;
import jakarta.persistence.Column;
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

import java.util.UUID;

@Entity
@Table(
        name = "league_standings",
        uniqueConstraints = @UniqueConstraint(name = "uk_league_standing_club", columnNames = {"league_id", "club_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class LeagueStanding {

    private static final int POINTS_FOR_WIN = 3;
    private static final int POINTS_FOR_DRAW = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @Column(nullable = false)
    private int played;

    @Column(nullable = false)
    private int wins;

    @Column(nullable = false)
    private int draws;

    @Column(nullable = false)
    private int losses;

    @Column(nullable = false)
    private int goalsFor;

    @Column(nullable = false)
    private int goalsAgainst;

    @Column(nullable = false)
    private int points;

    public static LeagueStanding zeroed(League league, Club club) {
        LeagueStanding standing = new LeagueStanding();
        standing.setLeague(league);
        standing.setClub(club);
        return standing;
    }

    public void recordResult(int scored, int conceded) {
        played++;
        goalsFor += scored;
        goalsAgainst += conceded;
        if (scored > conceded) {
            wins++;
            points += POINTS_FOR_WIN;
        } else if (scored == conceded) {
            draws++;
            points += POINTS_FOR_DRAW;
        } else {
            losses++;
        }
    }

    public int goalDifference() {
        return goalsFor - goalsAgainst;
    }
}
