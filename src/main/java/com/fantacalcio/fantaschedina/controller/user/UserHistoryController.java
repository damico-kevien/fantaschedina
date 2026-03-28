package com.fantacalcio.fantaschedina.controller.user;

import com.fantacalcio.fantaschedina.domain.entity.*;
import com.fantacalcio.fantaschedina.repository.CreditTransactionRepository;
import com.fantacalcio.fantaschedina.repository.LeagueMembershipRepository;
import com.fantacalcio.fantaschedina.repository.MatchdayRepository;
import com.fantacalcio.fantaschedina.repository.UserRepository;
import com.fantacalcio.fantaschedina.service.BetService;
import com.fantacalcio.fantaschedina.service.MatchdayService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/leagues")
@RequiredArgsConstructor
public class UserHistoryController {

    private final MatchdayService matchdayService;
    private final BetService betService;
    private final MatchdayRepository matchdayRepository;
    private final LeagueMembershipRepository leagueMembershipRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final UserRepository userRepository;

    @GetMapping("/{leagueId}/history")
    public String history(@PathVariable Long leagueId, Authentication authentication, Model model) {
        Long userId = userRepository.findByUsername(authentication.getName()).orElseThrow().getId();

        League league;
        try {
            league = matchdayService.getLeagueForMember(leagueId, userId);
        } catch (IllegalArgumentException e) {
            return "redirect:/dashboard";
        }

        LeagueMembership membership = leagueMembershipRepository
                .findByLeagueIdAndUserId(leagueId, userId).orElseThrow();
        FantaTeam myTeam = matchdayService.getFantaTeam(leagueId, userId).orElse(null);

        List<BetSlip> slips = myTeam != null
                ? betService.findSlipsForTeam(myTeam.getId())
                : List.of();

        // Build matchday map for display (number, status)
        Map<Long, Matchday> matchdayMap = slips.isEmpty() ? Map.of() :
                matchdayRepository.findAllById(
                        slips.stream().map(BetSlip::getMatchdayId).collect(Collectors.toSet())
                ).stream().collect(Collectors.toMap(Matchday::getId, m -> m));

        List<CreditTransaction> transactions = creditTransactionRepository
                .findByLeagueMembershipIdOrderByCreatedAtDesc(membership.getId());

        model.addAttribute("league", league);
        model.addAttribute("membership", membership);
        model.addAttribute("myTeam", myTeam);
        model.addAttribute("slips", slips.stream()
                .sorted((a, b) -> {
                    Matchday ma = matchdayMap.get(a.getMatchdayId());
                    Matchday mb = matchdayMap.get(b.getMatchdayId());
                    if (ma == null || mb == null) return 0;
                    return mb.getNumber().compareTo(ma.getNumber());
                })
                .collect(Collectors.toList()));
        model.addAttribute("matchdayMap", matchdayMap);
        model.addAttribute("transactions", transactions);

        return "user/history";
    }
}