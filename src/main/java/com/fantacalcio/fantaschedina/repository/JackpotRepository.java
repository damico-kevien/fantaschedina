package com.fantacalcio.fantaschedina.repository;

import com.fantacalcio.fantaschedina.domain.entity.Jackpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JackpotRepository extends JpaRepository<Jackpot, Long> {

    Optional<Jackpot> findByLeagueId(Long leagueId);
}
