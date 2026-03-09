package com.fantacalcio.fantaschedina.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jackpots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jackpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long leagueId;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentAmount = 0;

    private Long lastUpdatedMatchdayId;
}
