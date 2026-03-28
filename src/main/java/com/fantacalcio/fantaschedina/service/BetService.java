package com.fantacalcio.fantaschedina.service;

import com.fantacalcio.fantaschedina.domain.entity.*;
import com.fantacalcio.fantaschedina.domain.enums.*;
import com.fantacalcio.fantaschedina.dto.BetPickRequest;
import com.fantacalcio.fantaschedina.dto.BetSlipRequest;
import com.fantacalcio.fantaschedina.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BetService {

    private static final Map<OutcomeType, Set<String>> VALID_OUTCOMES = Map.of(
            OutcomeType.RESULT_1X2,    Set.of("1", "X", "2"),
            OutcomeType.DOUBLE_CHANCE, Set.of("1X", "12", "X2"),
            OutcomeType.GOAL_NOGOAL,   Set.of("GG", "NG"),
            OutcomeType.OVER_UNDER,    Set.of("OVER", "UNDER")
    );

    private final BetSlipRepository betSlipRepository;
    private final BetPickRepository betPickRepository;
    private final BetTemplateRepository betTemplateRepository;
    private final MatchdayRepository matchdayRepository;
    private final MatchdayFixtureRepository matchdayFixtureRepository;
    private final LeagueMembershipRepository leagueMembershipRepository;
    private final FantaTeamRepository fantaTeamRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final LeagueRepository leagueRepository;
    private final MatchdayService matchdayService;

    /**
     * Submits a bet slip for the given user on the given matchday.
     * Throws IllegalArgumentException with a user-facing message on any validation failure.
     */
    @Transactional
    public BetSlip submit(Long leagueId, Long matchdayId, Long userId, BetSlipRequest request) {
        League league = leagueRepository.findById(leagueId).orElseThrow();
        Matchday matchday = matchdayRepository.findById(matchdayId).orElseThrow();

        // Matchday must be OPEN
        if (matchday.getStatus() != MatchdayStatus.OPEN) {
            throw new IllegalArgumentException("La giornata non è aperta alle scommesse.");
        }

        // Deadline must not have passed
        LocalDateTime deadline = matchdayService.effectiveDeadline(matchday, league.getBetDeadlineMinutes());
        if (deadline != null && LocalDateTime.now().isAfter(deadline)) {
            throw new IllegalArgumentException("La scadenza per questa giornata è già passata.");
        }

        // Resolve FantaTeam
        LeagueMembership membership = leagueMembershipRepository
                .findByLeagueIdAndUserId(leagueId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Non sei membro di questa lega."));
        FantaTeam fantaTeam = fantaTeamRepository
                .findByLeagueMembershipId(membership.getId())
                .orElseThrow(() -> new IllegalArgumentException("Nessuna squadra trovata per il tuo account."));

        // One slip per matchday
        if (betSlipRepository.existsByFantaTeamIdAndMatchdayId(fantaTeam.getId(), matchdayId)) {
            throw new IllegalArgumentException("Hai già inviato una schedina per questa giornata.");
        }

        // Validate picks against BetTemplate
        List<BetTemplate> templates = betTemplateRepository.findByLeagueIdOrderByOrderIndexAsc(leagueId);
        validatePicks(request.getPicks(), templates, matchdayId);

        // Save BetSlip
        BetSlip slip = BetSlip.builder()
                .matchdayId(matchdayId)
                .fantaTeamId(fantaTeam.getId())
                .submittedAt(LocalDateTime.now())
                .isAutoSubmitted(false)
                .isAdminModified(false)
                .status(BetSlipStatus.PENDING)
                .amountCharged(league.getMatchdayCost())
                .build();
        slip = betSlipRepository.save(slip);

        // Save BetPicks
        for (BetPickRequest pick : request.getPicks()) {
            betPickRepository.save(BetPick.builder()
                    .betSlipId(slip.getId())
                    .matchdayFixtureId(pick.getFixtureId())
                    .outcomeType(pick.getOutcomeType())
                    .pickedOutcome(pick.getPickedOutcome())
                    .build());
        }

        // Debit credits
        int newBalance = membership.getBalance() - league.getMatchdayCost();
        membership.setBalance(newBalance);
        leagueMembershipRepository.save(membership);

        creditTransactionRepository.save(CreditTransaction.builder()
                .leagueMembershipId(membership.getId())
                .matchdayId(matchdayId)
                .type(TransactionType.BET_CHARGE)
                .amount(-league.getMatchdayCost())
                .balanceAfter(newBalance)
                .createdAt(LocalDateTime.now())
                .note("Schedina giornata " + matchday.getNumber())
                .build());

        return slip;
    }

    private void validatePicks(List<BetPickRequest> picks, List<BetTemplate> templates, Long matchdayId) {
        if (picks == null || picks.isEmpty()) {
            throw new IllegalArgumentException("La schedina non contiene nessun pronostico.");
        }

        // All fixture IDs must belong to this matchday
        Set<Long> validFixtureIds = matchdayFixtureRepository.findByMatchdayId(matchdayId).stream()
                .map(MatchdayFixture::getId)
                .collect(Collectors.toSet());

        for (BetPickRequest pick : picks) {
            if (!validFixtureIds.contains(pick.getFixtureId())) {
                throw new IllegalArgumentException("Una partita selezionata non appartiene a questa giornata.");
            }
            Set<String> valid = VALID_OUTCOMES.get(pick.getOutcomeType());
            if (valid == null || !valid.contains(pick.getPickedOutcome())) {
                throw new IllegalArgumentException("Esito non valido: " + pick.getPickedOutcome());
            }
        }

        // Each fixture must appear exactly once across all picks
        Set<Long> pickedFixtureIds = picks.stream()
                .map(BetPickRequest::getFixtureId)
                .collect(Collectors.toSet());
        if (pickedFixtureIds.size() < picks.size()) {
            throw new IllegalArgumentException("Ogni partita può essere giocata una sola volta.");
        }
        if (!pickedFixtureIds.equals(validFixtureIds)) {
            throw new IllegalArgumentException("Devi giocare tutte le partite della giornata, una per una.");
        }

        // Count picks per OutcomeType and check against template
        Map<OutcomeType, Long> pickCounts = picks.stream()
                .collect(Collectors.groupingBy(BetPickRequest::getOutcomeType, Collectors.counting()));

        for (BetTemplate template : templates) {
            long actual = pickCounts.getOrDefault(template.getOutcomeType(), 0L);
            if (actual != template.getRequiredCount()) {
                throw new IllegalArgumentException(
                        "Pronostici di tipo " + template.getOutcomeType().name() +
                        ": richiesti " + template.getRequiredCount() + ", inviati " + actual + "."
                );
            }
        }

        // No extra pick types beyond what the template requires
        for (OutcomeType type : pickCounts.keySet()) {
            boolean inTemplate = templates.stream().anyMatch(t -> t.getOutcomeType() == type);
            if (!inTemplate) {
                throw new IllegalArgumentException("Tipo di pronostico non previsto: " + type.name());
            }
        }
    }

    @Transactional(readOnly = true)
    public BetSlip findSlip(Long fantaTeamId, Long matchdayId) {
        return betSlipRepository.findByFantaTeamIdAndMatchdayId(fantaTeamId, matchdayId).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<BetSlip> findSlipsForTeam(Long fantaTeamId) {
        return betSlipRepository.findByFantaTeamId(fantaTeamId);
    }

    @Transactional(readOnly = true)
    public List<BetPick> findPicks(Long betSlipId) {
        return betPickRepository.findByBetSlipId(betSlipId);
    }
}