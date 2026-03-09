package com.fantacalcio.fantaschedina.repository;

import com.fantacalcio.fantaschedina.domain.entity.BetTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BetTemplateRepository extends JpaRepository<BetTemplate, Long> {

    List<BetTemplate> findByLeagueIdOrderByOrderIndexAsc(Long leagueId);

    void deleteByLeagueId(Long leagueId);
}
