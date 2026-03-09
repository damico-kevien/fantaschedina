package com.fantacalcio.fantaschedina.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "league_memberships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long leagueId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Builder.Default
    private Integer balance = 0;

    @Column(nullable = false)
    private LocalDateTime joinedAt;
}
