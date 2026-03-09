package com.fantacalcio.fantaschedina.domain.entity;

import com.fantacalcio.fantaschedina.domain.enums.OutcomeType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bet_picks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BetPick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long betSlipId;

    @Column(nullable = false)
    private Long matchdayFixtureId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OutcomeType outcomeType;

    @Column(nullable = false, length = 10)
    private String pickedOutcome;

    private Boolean isCorrect;
}
