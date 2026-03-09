package com.fantacalcio.fantaschedina.repository;

import com.fantacalcio.fantaschedina.domain.entity.BetSlip;
import com.fantacalcio.fantaschedina.domain.enums.BetSlipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BetSlipRepository extends JpaRepository<BetSlip, Long> {

    Optional<BetSlip> findByFantaTeamIdAndMatchdayId(Long fantaTeamId, Long matchdayId);

    List<BetSlip> findByMatchdayId(Long matchdayId);

    List<BetSlip> findByMatchdayIdAndStatus(Long matchdayId, BetSlipStatus status);

    List<BetSlip> findByFantaTeamId(Long fantaTeamId);

    boolean existsByFantaTeamIdAndMatchdayId(Long fantaTeamId, Long matchdayId);
}
