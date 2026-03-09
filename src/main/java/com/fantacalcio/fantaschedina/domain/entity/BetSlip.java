package com.fantacalcio.fantaschedina.domain.entity;

import com.fantacalcio.fantaschedina.domain.enums.BetSlipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "bet_slips",
    uniqueConstraints = @UniqueConstraint(columnNames = {"fanta_team_id", "matchday_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BetSlip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matchday_id", nullable = false)
    private Long matchdayId;

    @Column(name = "fanta_team_id", nullable = false)
    private Long fantaTeamId;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAutoSubmitted = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAdminModified = false;

    private LocalDateTime adminModifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BetSlipStatus status;

    @Column(nullable = false)
    private Integer amountCharged;
}
