package com.fantacalcio.fantaschedina.controller.admin;

import com.fantacalcio.fantaschedina.domain.entity.BetSlip;
import com.fantacalcio.fantaschedina.domain.entity.BetPick;
import com.fantacalcio.fantaschedina.dto.BetSlipRequest;
import com.fantacalcio.fantaschedina.repository.UserRepository;
import com.fantacalcio.fantaschedina.service.AdminBetSlipService;
import com.fantacalcio.fantaschedina.service.LeagueService;
import com.fantacalcio.fantaschedina.service.MatchdayService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/leagues/{leagueId}")
@RequiredArgsConstructor
public class AdminBetSlipController {

    private final AdminBetSlipService adminBetSlipService;
    private final LeagueService leagueService;
    private final MatchdayService matchdayService;
    private final UserRepository userRepository;

    private Long adminId(UserDetails user) {
        return userRepository.findByUsername(user.getUsername()).orElseThrow().getId();
    }

    @GetMapping("/matchdays/{matchdayId}/slips")
    public String listSlips(@PathVariable Long leagueId,
                            @PathVariable Long matchdayId,
                            @AuthenticationPrincipal UserDetails user,
                            Model model) {
        leagueService.findByIdForAdmin(leagueId, adminId(user));
        List<BetSlip> slips = adminBetSlipService.getSlipsForMatchday(matchdayId);
        Map<Long, String> teamNames = adminBetSlipService.getTeamNamesForSlips(slips);

        model.addAttribute("league", leagueService.findById(leagueId));
        model.addAttribute("matchday", matchdayService.getMatchday(matchdayId, leagueId));
        model.addAttribute("slips", slips);
        model.addAttribute("teamNames", teamNames);
        return "admin/leagues/slip-list";
    }

    @GetMapping("/slips/{slipId}/edit")
    public String editForm(@PathVariable Long leagueId,
                           @PathVariable Long slipId,
                           @AuthenticationPrincipal UserDetails user,
                           Model model) {
        leagueService.findByIdForAdmin(leagueId, adminId(user));
        BetSlip slip = adminBetSlipService.getSlip(slipId);
        Map<Long, BetPick> picksByFixture = adminBetSlipService.getPicksByFixture(slipId);

        model.addAttribute("league", leagueService.findById(leagueId));
        model.addAttribute("matchday", matchdayService.getMatchday(slip.getMatchdayId(), leagueId));
        model.addAttribute("slip", slip);
        model.addAttribute("pickSlots", adminBetSlipService.getPickSlots(leagueId));
        model.addAttribute("fixtures", matchdayService.getFixtures(slip.getMatchdayId()));
        model.addAttribute("teamNames", adminBetSlipService.getTeamNames(leagueId));
        model.addAttribute("currentPicks", adminBetSlipService.getPicksOrdered(slipId));
        return "admin/leagues/slip-edit";
    }

    @PostMapping("/slips/{slipId}/edit")
    public String saveEdit(@PathVariable Long leagueId,
                           @PathVariable Long slipId,
                           @ModelAttribute BetSlipRequest request,
                           @RequestParam(required = false) String note,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        Long adminUserId = adminId(userDetails);
        leagueService.findByIdForAdmin(leagueId, adminUserId);
        BetSlip slip = adminBetSlipService.getSlip(slipId);

        try {
            adminBetSlipService.modifySlip(slipId, adminUserId, request, note);
            redirectAttributes.addFlashAttribute("success", "Schedina modificata con successo.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leagues/" + leagueId + "/matchdays/" + slip.getMatchdayId() + "/slips";
    }
}
