package com.fantacalcio.fantaschedina.controller.user;

import com.fantacalcio.fantaschedina.domain.entity.*;
import com.fantacalcio.fantaschedina.repository.MatchdayRepository;
import com.fantacalcio.fantaschedina.repository.UserRepository;
import com.fantacalcio.fantaschedina.repository.BetSlipRepository;
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
public class BetSlipDetailController {

    private final BetService betService;
    private final MatchdayService matchdayService;
    private final BetSlipRepository betSlipRepository;
    private final MatchdayRepository matchdayRepository;
    private final UserRepository userRepository;

    @GetMapping("/{leagueId}/slips/{slipId}")
    public String detail(@PathVariable Long leagueId, @PathVariable Long slipId,
                         Authentication authentication, Model model) {
        Long userId = userRepository.findByUsername(authentication.getName()).orElseThrow().getId();

        League league;
        try {
            league = matchdayService.getLeagueForMember(leagueId, userId);
        } catch (IllegalArgumentException e) {
            return "redirect:/dashboard";
        }

        BetSlip slip = betSlipRepository.findById(slipId).orElse(null);
        if (slip == null) {
            return "redirect:/leagues/" + leagueId + "/history";
        }

        // Verify the slip belongs to the user's team in this league
        FantaTeam myTeam = matchdayService.getFantaTeam(leagueId, userId).orElse(null);
        if (myTeam == null || !slip.getFantaTeamId().equals(myTeam.getId())) {
            return "redirect:/leagues/" + leagueId + "/history";
        }

        Matchday matchday = matchdayRepository.findById(slip.getMatchdayId()).orElseThrow();
        List<BetPick> picks = betService.findPicks(slipId);
        List<MatchdayFixture> fixtures = matchdayService.getFixtures(slip.getMatchdayId());
        Map<Long, String> teamNames = matchdayService.getTeamNames(leagueId);
        Map<Long, BetPick> picksByFixture = picks.stream()
                .collect(Collectors.toMap(BetPick::getMatchdayFixtureId, p -> p));

        model.addAttribute("league", league);
        model.addAttribute("matchday", matchday);
        model.addAttribute("slip", slip);
        model.addAttribute("picks", picks);
        model.addAttribute("fixtures", fixtures);
        model.addAttribute("teamNames", teamNames);
        model.addAttribute("picksByFixture", picksByFixture);
        model.addAttribute("myTeam", myTeam);

        return "user/slip-detail";
    }
}
