package com.fantacalcio.fantaschedina.scheduler;

import com.fantacalcio.fantaschedina.domain.entity.League;
import com.fantacalcio.fantaschedina.domain.entity.Matchday;
import com.fantacalcio.fantaschedina.domain.enums.MatchdayStatus;
import com.fantacalcio.fantaschedina.repository.LeagueRepository;
import com.fantacalcio.fantaschedina.repository.MatchdayRepository;
import com.fantacalcio.fantaschedina.service.MatchdayClosingService;
import com.fantacalcio.fantaschedina.service.MatchdayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchdaySafetyNetScheduler {

    private final MatchdayRepository matchdayRepository;
    private final LeagueRepository leagueRepository;
    private final MatchdayService matchdayService;
    private final MatchdayClosingService matchdayClosingService;

    /**
     * Fallback: closes any OPEN matchday whose effective deadline has passed.
     * Handles the gap between app restart and Quartz job re-execution.
     */
    @Scheduled(fixedDelay = 60_000)
    public void closeOverdueMatchdays() {
        List<Matchday> openMatchdays = matchdayRepository.findByStatus(MatchdayStatus.OPEN);
        for (Matchday matchday : openMatchdays) {
            League league = leagueRepository.findById(matchday.getLeagueId()).orElse(null);
            if (league == null) continue;

            LocalDateTime deadline = matchdayService.effectiveDeadline(matchday, league.getBetDeadlineMinutes());
            if (deadline != null && LocalDateTime.now().isAfter(deadline)) {
                log.info("Safety net: closing overdue matchday {}", matchday.getId());
                matchdayClosingService.closeAndAutoSubmit(matchday.getId());
            }
        }
    }
}