package com.fantacalcio.fantaschedina.controller;

import com.fantacalcio.fantaschedina.repository.UserRepository;
import com.fantacalcio.fantaschedina.service.MatchdayService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserRepository userRepository;
    private final MatchdayService matchdayService;

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        Long userId = userRepository.findByUsername(authentication.getName()).orElseThrow().getId();
        model.addAttribute("leagueCards", matchdayService.getLeaguesForUser(userId));
        return "dashboard";
    }
}
