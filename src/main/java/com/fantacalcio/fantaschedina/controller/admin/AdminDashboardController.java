package com.fantacalcio.fantaschedina.controller.admin;

import com.fantacalcio.fantaschedina.repository.UserRepository;
import com.fantacalcio.fantaschedina.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final UserRepository userRepository;

    private Long adminId(UserDetails user) {
        return userRepository.findByUsername(user.getUsername()).orElseThrow().getId();
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails user, Model model) {
        model.addAttribute("leagueCards", adminDashboardService.buildLeagueCards(adminId(user)));
        model.addAttribute("expiringInvites", adminDashboardService.getExpiringInvites());
        return "admin/dashboard";
    }
}
