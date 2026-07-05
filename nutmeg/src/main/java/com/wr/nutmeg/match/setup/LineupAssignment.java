package com.wr.nutmeg.match.setup;

import com.wr.nutmeg.match.tactics.FormationSlot;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class LineupAssignment {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormationSlot slot;

    @Column(nullable = false)
    private UUID playerId;
}
