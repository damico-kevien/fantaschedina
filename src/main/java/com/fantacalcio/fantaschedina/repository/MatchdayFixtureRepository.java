package com.fantacalcio.fantaschedina.repository;

import com.fantacalcio.fantaschedina.domain.entity.MatchdayFixture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchdayFixtureRepository extends JpaRepository<MatchdayFixture, Long> {

    List<MatchdayFixture> findByMatchdayId(Long matchdayId);

    boolean existsByMatchdayIdAndResultLoadedFalse(Long matchdayId);

    long countByMatchdayId(Long matchdayId);
}
