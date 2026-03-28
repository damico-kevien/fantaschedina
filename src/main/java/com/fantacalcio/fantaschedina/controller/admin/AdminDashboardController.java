package com.fantacalcio.fantaschedina.controller.admin;

import com.fantacalcio.fantaschedina.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("leagueCards", adminDashboardService.buildLeagueCards());
        model.addAttribute("expiringInvites", adminDashboardService.getExpiringInvites());
        return "admin/dashboard";
    }
}