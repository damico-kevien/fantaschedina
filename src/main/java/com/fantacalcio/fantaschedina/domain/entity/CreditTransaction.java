package com.fantacalcio.fantaschedina.domain.entity;

import com.fantacalcio.fantaschedina.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "credit_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long leagueMembershipId;

    private Long matchdayId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer balanceAfter;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 500)
    private String note;
}
