package com.fantacalcio.fantaschedina.repository;

import com.fantacalcio.fantaschedina.domain.entity.BetPick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetPickRepository extends JpaRepository<BetPick, Long> {

    List<BetPick> findByBetSlipId(Long betSlipId);

    void deleteByBetSlipId(Long betSlipId);
}
