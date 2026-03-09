package com.fantacalcio.fantaschedina.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "matchday_fixtures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchdayFixture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long matchdayId;

    @Column(nullable = false)
    private Long homeFantaTeamId;

    @Column(nullable = false)
    private Long awayFantaTeamId;

    private Integer homeScore;

    private Integer awayScore;

    @Column(nullable = false)
    @Builder.Default
    private Boolean resultLoaded = false;
}
