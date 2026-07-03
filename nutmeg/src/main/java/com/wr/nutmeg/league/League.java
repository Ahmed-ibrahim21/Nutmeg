package com.wr.nutmeg.league;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.common.enums.LeagueStatus;
import com.wr.nutmeg.fixture.Fixture;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "leagues")
@Getter
@Setter
@NoArgsConstructor
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String country;

    private int tier;

    private int currentRound;

    private int totalRounds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeagueStatus status = LeagueStatus.UPCOMING;

    @OneToMany(mappedBy = "league")
    private List<Club> clubs = new ArrayList<>();

    @OneToMany(mappedBy = "league")
    private List<Fixture> fixtures = new ArrayList<>();
}
