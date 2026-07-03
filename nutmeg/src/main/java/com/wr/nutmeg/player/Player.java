package com.wr.nutmeg.player;

import com.wr.nutmeg.club.Club;
import com.wr.nutmeg.common.enums.PlayerRole;
import com.wr.nutmeg.common.enums.Position;
import com.wr.nutmeg.common.enums.PreferredFoot;
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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String nationality;

    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position position;

    @Enumerated(EnumType.STRING)
    private PreferredFoot preferredFoot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    private String avatarUrl;

    @Column(nullable = false)
    private int pace;

    @Column(nullable = false)
    private int shooting;

    @Column(nullable = false)
    private int passing;

    @Column(nullable = false)
    private int dribbling;

    @Column(nullable = false)
    private int defending;

    @Column(nullable = false)
    private int physical;

    @Column(nullable = false)
    private int stamina;

    private int currentFitness;

    private int morale;

    private int potential;

    private long marketValue;

    private long weeklyWage;

    private LocalDate contractExpiry;

    private Integer jerseyNumber;

    private boolean suspended;

    private boolean injured;

    private int appearances;

    private int goals;

    private int assists;

    private int yellowCards;

    private int redCards;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getOverallRating() {
        PlayerRole role = position.getRole();
        if (role == PlayerRole.GK) {
            return average(defending, physical, stamina);
        }
        if (role == PlayerRole.DEF) {
            return average(defending, physical, pace, passing);
        }
        if (role == PlayerRole.MID) {
            return average(passing, dribbling, stamina, defending);
        }
        return average(shooting, pace, dribbling, passing);
    }

    private static int average(int... values) {
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        return sum / values.length;
    }
}
