package com.fantacalcio.fantaschedina.repository;

import com.fantacalcio.fantaschedina.domain.entity.CreditTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, Long> {

    List<CreditTransaction> findByLeagueMembershipIdOrderByCreatedAtDesc(Long leagueMembershipId);

    List<CreditTransaction> findByMatchdayId(Long matchdayId);
}
