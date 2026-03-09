package com.fantacalcio.fantaschedina.repository;

import com.fantacalcio.fantaschedina.domain.entity.Matchday;
import com.fantacalcio.fantaschedina.domain.enums.MatchdayStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchdayRepository extends JpaRepository<Matchday, Long> {

    List<Matchday> findByLeagueIdOrderByNumberAsc(Long leagueId);

    List<Matchday> findByLeagueIdAndStatus(Long leagueId, MatchdayStatus status);

    List<Matchday> findByStatus(MatchdayStatus status);

    Optional<Matchday> findByLeagueIdAndNumber(Long leagueId, Integer number);

    long countByLeagueIdAndStatusIn(Long leagueId, List<MatchdayStatus> statuses);
}
