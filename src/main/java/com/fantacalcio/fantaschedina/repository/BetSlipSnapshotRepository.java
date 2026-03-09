package com.fantacalcio.fantaschedina.repository;

import com.fantacalcio.fantaschedina.domain.entity.BetSlipSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetSlipSnapshotRepository extends JpaRepository<BetSlipSnapshot, Long> {

    List<BetSlipSnapshot> findByBetSlipIdOrderBySnapshotAtDesc(Long betSlipId);
}
