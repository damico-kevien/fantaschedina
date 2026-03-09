package com.fantacalcio.fantaschedina.controller.user;

import com.fantacalcio.fantaschedina.domain.entity.Invite;
import com.fantacalcio.fantaschedina.domain.entity.User;
import com.fantacalcio.fantaschedina.exception.InvalidInviteException;
import com.fantacalcio.fantaschedina.repository.UserRepository;
import com.fantacalcio.fantaschedina.service.InviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/invite")
@RequiredArgsConstructor
public class InviteController {

    private final InviteService inviteService;
    private final UserRepository userRepository;

    @GetMapping("/accept")
    public String acceptInvite(@RequestParam String token,
                               Authentication authentication,
                               Model model) {
        Invite invite;
        try {
            invite = inviteService.findValidInvite(token);
        } catch (InvalidInviteException e) {
            model.addAttribute("error", e.getMessage());
            return "invite-error";
        }

        // New user flow
        if (invite.getUserId() == null) {
            return "redirect:/register?token=" + token;
        }

        // Existing user flow — must be authenticated
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
        if (!currentUser.getId().equals(invite.getUserId())) {
            model.addAttribute("error", "Questo invito non è destinato a te.");
            return "invite-error";
        }

        model.addAttribute("token", token);
        return "invite-accept";
    }

    @PostMapping("/accept")
    public String confirmAccept(@RequestParam String token,
                                @RequestParam String fantaTeamName,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();
        try {
            inviteService.acceptForExistingUser(token, currentUser.getId(), fantaTeamName);
            redirectAttributes.addFlashAttribute("success", "Sei entrato nella lega!");
        } catch (InvalidInviteException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }
}
