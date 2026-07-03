package com.wr.nutmeg.club;

import com.wr.nutmeg.league.League;
import com.wr.nutmeg.manager.Manager;
import com.wr.nutmeg.player.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "clubs")
@Getter
@Setter
@NoArgsConstructor
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    private String shortName;

    private String logoUrl;

    private String stadiumName;

    private int stadiumCapacity;

    private long budget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;

    @OneToOne(mappedBy = "club", fetch = FetchType.LAZY)
    private Manager manager;

    @OneToMany(mappedBy = "club")
    private List<Player> squad = new ArrayList<>();
}
