package com.fantacalcio.fantaschedina.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bet_slip_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BetSlipSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long betSlipId;

    @Column(nullable = false)
    private LocalDateTime snapshotAt;

    @Column(nullable = false)
    private Long adminUserId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String picksJson;

    @Column(length = 500)
    private String note;
}
