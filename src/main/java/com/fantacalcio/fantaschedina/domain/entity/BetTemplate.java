package com.fantacalcio.fantaschedina.domain.entity;

import com.fantacalcio.fantaschedina.domain.enums.OutcomeType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bet_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BetTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long leagueId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OutcomeType outcomeType;

    @Column(nullable = false)
    private Integer requiredCount;

    private Double overUnderThreshold;

    @Column(nullable = false)
    private Integer orderIndex;
}
