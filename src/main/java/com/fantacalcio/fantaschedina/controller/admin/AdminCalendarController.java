package com.fantacalcio.fantaschedina.controller.admin;

import com.fantacalcio.fantaschedina.dto.MatchdayScheduleRequest;
import com.fantacalcio.fantaschedina.repository.UserRepository;
import com.fantacalcio.fantaschedina.service.CalendarService;
import com.fantacalcio.fantaschedina.service.LeagueService;
import com.fantacalcio.fantaschedina.service.MatchdayService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/leagues/{leagueId}/calendar")
@RequiredArgsConstructor
public class AdminCalendarController {

    private final CalendarService calendarService;
    private final LeagueService leagueService;
    private final MatchdayService matchdayService;
    private final UserRepository userRepository;

    private static final String SESSION_KEY = "pendingCsvImport";

    private Long adminId(UserDetails user) {
        return userRepository.findByUsername(user.getUsername()).orElseThrow().getId();
    }

    @GetMapping
    public String calendar(@PathVariable Long leagueId,
                           @AuthenticationPrincipal UserDetails user,
                           Model model) {
        leagueService.findByIdForAdmin(leagueId, adminId(user));
        var matchdays = calendarService.findMatchdaysByLeague(leagueId);

        model.addAttribute("league", leagueService.findById(leagueId));
        model.addAttribute("matchdays", matchdays);
        model.addAttribute("fixturesByMatchday", calendarService.findFixturesGroupedByMatchday(matchdays));
        model.addAttribute("teamNames", matchdayService.getTeamNames(leagueId));
        return "admin/leagues/calendar";
    }

    @PostMapping("/import")
    public String importCsv(@PathVariable Long leagueId,
                            @AuthenticationPrincipal UserDetails user,
                            @RequestParam("file") MultipartFile file,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        leagueService.findByIdForAdmin(leagueId, adminId(user));
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Seleziona un file CSV");
            return "redirect:/admin/leagues/" + leagueId + "/calendar";
        }
        try {
            byte[] csvBytes = file.getBytes();
            List<Integer> conflicts = calendarService.importCsv(leagueId, csvBytes, false);
            if (!conflicts.isEmpty()) {
                session.setAttribute(SESSION_KEY, csvBytes);
                redirectAttributes.addFlashAttribute("conflictMatchdays", conflicts);
            } else {
                redirectAttributes.addFlashAttribute("success", "Calendario importato con successo");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leagues/" + leagueId + "/calendar";
    }

    @PostMapping("/import/confirm")
    public String confirmImport(@PathVariable Long leagueId,
                                @AuthenticationPrincipal UserDetails user,
                                @RequestParam(defaultValue = "false") boolean overwrite,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        leagueService.findByIdForAdmin(leagueId, adminId(user));
        byte[] csvBytes = (byte[]) session.getAttribute(SESSION_KEY);
        if (csvBytes == null) {
            redirectAttributes.addFlashAttribute("error", "Sessione scaduta, ricarica il CSV");
            return "redirect:/admin/leagues/" + leagueId + "/calendar";
        }
        try {
            calendarService.importCsv(leagueId, csvBytes, overwrite);
            String msg = overwrite ? "Calendario importato con sovrascrittura" : "Calendario importato (giornate esistenti saltate)";
            redirectAttributes.addFlashAttribute("success", msg);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } finally {
            session.removeAttribute(SESSION_KEY);
        }
        return "redirect:/admin/leagues/" + leagueId + "/calendar";
    }

    @PostMapping("/matchday")
    public String addMatchday(@PathVariable Long leagueId,
                              @AuthenticationPrincipal UserDetails user,
                              @RequestParam Integer number,
                              RedirectAttributes redirectAttributes) {
        leagueService.findByIdForAdmin(leagueId, adminId(user));
        try {
            calendarService.addMatchday(leagueId, number);
            redirectAttributes.addFlashAttribute("success", "Giornata " + number + " creata");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leagues/" + leagueId + "/calendar";
    }

    @PostMapping("/{matchdayId}/fixture")
    public String addFixture(@PathVariable Long leagueId,
                             @PathVariable Long matchdayId,
                             @AuthenticationPrincipal UserDetails user,
                             @RequestParam Long homeTeamId,
                             @RequestParam Long awayTeamId,
                             RedirectAttributes redirectAttributes) {
        leagueService.findByIdForAdmin(leagueId, adminId(user));
        try {
            calendarService.addFixture(leagueId, matchdayId, homeTeamId, awayTeamId);
            redirectAttributes.addFlashAttribute("success", "Partita aggiunta");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leagues/" + leagueId + "/calendar";
    }

    @PostMapping("/{matchdayId}/schedule")
    public String schedule(@PathVariable Long leagueId,
                           @PathVariable Long matchdayId,
                           @AuthenticationPrincipal UserDetails user,
                           @ModelAttribute MatchdayScheduleRequest request,
                           RedirectAttributes redirectAttributes) {
        leagueService.findByIdForAdmin(leagueId, adminId(user));
        try {
            calendarService.scheduleMatchday(matchdayId, request);
            redirectAttributes.addFlashAttribute("success", "Date aggiornate");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/leagues/" + leagueId + "/calendar";
    }
}