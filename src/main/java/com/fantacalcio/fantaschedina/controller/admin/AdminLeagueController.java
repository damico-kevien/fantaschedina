package com.fantacalcio.fantaschedina.controller.admin;

import com.fantacalcio.fantaschedina.dto.BetTemplateForm;
import com.fantacalcio.fantaschedina.dto.LeagueRequest;
import com.fantacalcio.fantaschedina.repository.UserRepository;
import com.fantacalcio.fantaschedina.service.BetTemplateService;
import com.fantacalcio.fantaschedina.service.LeagueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/leagues")
@RequiredArgsConstructor
public class AdminLeagueController {

    private final LeagueService leagueService;
    private final BetTemplateService betTemplateService;
    private final UserRepository userRepository;

    private Long adminId(UserDetails user) {
        return userRepository.findByUsername(user.getUsername()).orElseThrow().getId();
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails user, Model model) {
        model.addAttribute("leagues", leagueService.findAllForAdmin(adminId(user)));
        return "admin/leagues/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("leagueRequest", new LeagueRequest());
        return "admin/leagues/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute LeagueRequest leagueRequest,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails user,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/leagues/form";
        }
        var league = leagueService.create(leagueRequest, adminId(user));
        redirectAttributes.addFlashAttribute("success", "Lega creata con successo");
        return "redirect:/admin/leagues/" + league.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails user,
                         Model model) {
        model.addAttribute("league", leagueService.findByIdForAdmin(id, adminId(user)));
        model.addAttribute("betTemplateForm", betTemplateService.buildForm(id));
        model.addAttribute("betTemplates", betTemplateService.findByLeague(id));
        return "admin/leagues/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails user,
                           Model model) {
        var league = leagueService.findByIdForAdmin(id, adminId(user));
        var request = new LeagueRequest();
        request.setName(league.getName());
        request.setSeason(league.getSeason());
        request.setMatchdayCost(league.getMatchdayCost());
        request.setJackpotStart(league.getJackpotStart());
        request.setBetDeadlineMinutes(league.getBetDeadlineMinutes());
        model.addAttribute("leagueRequest", request);
        model.addAttribute("leagueId", id);
        return "admin/leagues/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute LeagueRequest leagueRequest,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetails user,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("leagueId", id);
            return "admin/leagues/form";
        }
        leagueService.findByIdForAdmin(id, adminId(user));
        leagueService.update(id, leagueRequest);
        redirectAttributes.addFlashAttribute("success", "Lega aggiornata");
        return "redirect:/admin/leagues/" + id;
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails user,
                           RedirectAttributes redirectAttributes) {
        try {
            leagueService.findByIdForAdmin(id, adminId(user));
            leagueService.activate(id);
            redirectAttributes.addFlashAttribute("success", "Lega attivata");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leagues/" + id;
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id,
                        @AuthenticationPrincipal UserDetails user,
                        RedirectAttributes redirectAttributes) {
        try {
            leagueService.findByIdForAdmin(id, adminId(user));
            leagueService.close(id);
            redirectAttributes.addFlashAttribute("success", "Lega chiusa");
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leagues/" + id;
    }

    @PostMapping("/{id}/bet-template")
    public String saveBetTemplate(@PathVariable Long id,
                                  @ModelAttribute BetTemplateForm betTemplateForm,
                                  @AuthenticationPrincipal UserDetails user,
                                  RedirectAttributes redirectAttributes) {
        leagueService.findByIdForAdmin(id, adminId(user));
        betTemplateService.save(id, betTemplateForm);
        redirectAttributes.addFlashAttribute("success", "Template schedina salvato");
        return "redirect:/admin/leagues/" + id;
    }
}