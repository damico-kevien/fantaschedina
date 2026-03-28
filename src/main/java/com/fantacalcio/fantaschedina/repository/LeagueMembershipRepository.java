package com.fantacalcio.fantaschedina.repository;

import com.fantacalcio.fantaschedina.domain.entity.LeagueMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeagueMembershipRepository extends JpaRepository<LeagueMembership, Long> {

    List<LeagueMembership> findByLeagueId(Long leagueId);

    Optional<LeagueMembership> findByLeagueIdAndUserId(Long leagueId, Long userId);

    boolean existsByLeagueIdAndUserId(Long leagueId, Long userId);

    List<LeagueMembership> findByUserId(Long userId);

    long countByLeagueId(Long leagueId);
}
