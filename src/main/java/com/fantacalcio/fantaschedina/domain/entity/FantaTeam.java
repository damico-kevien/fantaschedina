package com.fantacalcio.fantaschedina.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fanta_teams")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FantaTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long leagueId;

    @Column(nullable = false)
    private Long leagueMembershipId;

    @Column(nullable = false)
    private String name;
}
