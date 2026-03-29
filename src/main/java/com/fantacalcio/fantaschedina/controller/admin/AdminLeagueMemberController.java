package com.fantacalcio.fantaschedina.controller.admin;

import com.fantacalcio.fantaschedina.repository.UserRepository;
import com.fantacalcio.fantaschedina.service.AdminLeagueMemberService;
import com.fantacalcio.fantaschedina.service.LeagueService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/leagues/{leagueId}/members")
@RequiredArgsConstructor
public class AdminLeagueMemberController {

    private final LeagueService leagueService;
    private final AdminLeagueMemberService memberService;
    private final UserRepository userRepository;

    private Long adminId(UserDetails user) {
        return userRepository.findByUsername(user.getUsername()).orElseThrow().getId();
    }

    @GetMapping
    public String members(@PathVariable Long leagueId,
                          @AuthenticationPrincipal UserDetails user,
                          Model model) {
        leagueService.findByIdForAdmin(leagueId, adminId(user));
        model.addAttribute("league", leagueService.findById(leagueId));
        model.addAttribute("members", memberService.getMembers(leagueId));
        model.addAttribute("jackpot", memberService.getJackpot(leagueId));
        model.addAttribute("auditLog", memberService.getAuditLog(leagueId));
        return "admin/leagues/members";
    }

    @PostMapping("/{membershipId}/adjust")
    public String adjustMember(@PathVariable Long leagueId,
                               @PathVariable Long membershipId,
                               @AuthenticationPrincipal UserDetails user,
                               @RequestParam int delta,
                               @RequestParam(required = false) String note,
                               RedirectAttributes redirectAttributes) {
        leagueService.findByIdForAdmin(leagueId, adminId(user));
        try {
            memberService.adjustMemberBalance(membershipId, delta, note);
            redirectAttributes.addFlashAttribute("success", "Crediti aggiornati.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leagues/" + leagueId + "/members";
    }

    @PostMapping("/jackpot/adjust")
    public String adjustJackpot(@PathVariable Long leagueId,
                                @AuthenticationPrincipal UserDetails user,
                                @RequestParam int newAmount,
                                @RequestParam(required = false) String note,
                                RedirectAttributes redirectAttributes) {
        leagueService.findByIdForAdmin(leagueId, adminId(user));
        try {
            memberService.adjustJackpot(leagueId, newAmount, note);
            redirectAttributes.addFlashAttribute("success", "Jackpot aggiornato.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leagues/" + leagueId + "/members";
    }
}
