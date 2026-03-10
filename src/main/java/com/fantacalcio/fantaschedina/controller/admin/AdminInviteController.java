package com.fantacalcio.fantaschedina.controller.admin;

import com.fantacalcio.fantaschedina.domain.entity.League;
import com.fantacalcio.fantaschedina.dto.InviteRequest;
import com.fantacalcio.fantaschedina.repository.InviteRepository;
import com.fantacalcio.fantaschedina.repository.LeagueRepository;
import com.fantacalcio.fantaschedina.service.InviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/invites")
@RequiredArgsConstructor
public class AdminInviteController {

    private final InviteService inviteService;
    private final LeagueRepository leagueRepository;
    private final InviteRepository inviteRepository;

    @GetMapping
    public String listInvites(Model model) {
        model.addAttribute("invites", inviteRepository.findAll());
        model.addAttribute("leagues", leagueRepository.findAll());
        model.addAttribute("leagueNames", buildLeagueNamesMap());
        model.addAttribute("inviteRequest", new InviteRequest());
        return "admin/invites";
    }

    @PostMapping("/send")
    public String sendInvite(@Valid @ModelAttribute InviteRequest inviteRequest,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("invites", inviteRepository.findAll());
            model.addAttribute("leagues", leagueRepository.findAll());
            model.addAttribute("leagueNames", buildLeagueNamesMap());
            return "admin/invites";
        }

        try {
            inviteService.createInvite(inviteRequest.getLeagueId(), inviteRequest.getEmail());
            redirectAttributes.addFlashAttribute("success",
                "Invito inviato a " + inviteRequest.getEmail());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/invites";
    }

    @PostMapping("/{id}/revoke")
    public String revokeInvite(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            inviteService.revokeInvite(id);
            redirectAttributes.addFlashAttribute("success", "Invito revocato");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/invites";
    }

    private Map<Long, String> buildLeagueNamesMap() {
        return leagueRepository.findAll().stream()
            .collect(Collectors.toMap(League::getId, l -> l.getName() + " (" + l.getSeason() + ")"));
    }
}
