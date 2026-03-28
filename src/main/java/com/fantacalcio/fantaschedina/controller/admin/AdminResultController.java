package com.fantacalcio.fantaschedina.controller.admin;

import com.fantacalcio.fantaschedina.domain.entity.League;
import com.fantacalcio.fantaschedina.domain.entity.Matchday;
import com.fantacalcio.fantaschedina.domain.entity.MatchdayFixture;
import com.fantacalcio.fantaschedina.domain.enums.MatchdayStatus;
import com.fantacalcio.fantaschedina.dto.FixtureResultRequest;
import com.fantacalcio.fantaschedina.dto.MatchdayResultRequest;
import com.fantacalcio.fantaschedina.repository.FantaTeamRepository;
import com.fantacalcio.fantaschedina.repository.LeagueRepository;
import com.fantacalcio.fantaschedina.repository.MatchdayFixtureRepository;
import com.fantacalcio.fantaschedina.repository.MatchdayRepository;
import com.fantacalcio.fantaschedina.service.MatchdayProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/leagues/{leagueId}/matchdays/{matchdayId}/results")
@RequiredArgsConstructor
public class AdminResultController {

    private final LeagueRepository leagueRepository;
    private final MatchdayRepository matchdayRepository;
    private final MatchdayFixtureRepository matchdayFixtureRepository;
    private final FantaTeamRepository fantaTeamRepository;
    private final MatchdayProcessingService processingService;

    @GetMapping
    public String form(@PathVariable Long leagueId, @PathVariable Long matchdayId, Model model) {
        League league = leagueRepository.findById(leagueId).orElseThrow();
        Matchday matchday = matchdayRepository.findById(matchdayId).orElseThrow();

        if (matchday.getStatus() != MatchdayStatus.CLOSED) {
            return "redirect:/admin/leagues/" + leagueId + "/calendar";
        }

        List<MatchdayFixture> fixtures = matchdayFixtureRepository.findByMatchdayId(matchdayId);
        Map<Long, String> teamNames = fantaTeamRepository.findByLeagueId(leagueId).stream()
                .collect(Collectors.toMap(t -> t.getId(), t -> t.getName()));

        model.addAttribute("league", league);
        model.addAttribute("matchday", matchday);
        model.addAttribute("fixtures", fixtures);
        model.addAttribute("teamNames", teamNames);
        return "admin/leagues/results";
    }

    @PostMapping
    public String submit(@PathVariable Long leagueId, @PathVariable Long matchdayId,
                         @ModelAttribute MatchdayResultRequest request,
                         RedirectAttributes redirectAttributes) {
        Matchday matchday = matchdayRepository.findById(matchdayId).orElseThrow();

        if (matchday.getStatus() != MatchdayStatus.CLOSED) {
            redirectAttributes.addFlashAttribute("error", "La giornata non è in stato CLOSED.");
            return "redirect:/admin/leagues/" + leagueId + "/calendar";
        }

        // Validate all scores are present
        for (FixtureResultRequest f : request.getFixtures()) {
            if (f.getHomeScore() == null || f.getAwayScore() == null) {
                redirectAttributes.addFlashAttribute("error", "Inserisci tutti i risultati prima di confermare.");
                return "redirect:/admin/leagues/" + leagueId + "/matchdays/" + matchdayId + "/results";
            }
        }

        // Save scores
        Map<Long, MatchdayFixture> fixtureMap = matchdayFixtureRepository.findByMatchdayId(matchdayId)
                .stream().collect(Collectors.toMap(MatchdayFixture::getId, f -> f));

        for (FixtureResultRequest f : request.getFixtures()) {
            MatchdayFixture fixture = fixtureMap.get(f.getFixtureId());
            if (fixture == null) continue;
            fixture.setHomeScore(f.getHomeScore());
            fixture.setAwayScore(f.getAwayScore());
            fixture.setResultLoaded(true);
            matchdayFixtureRepository.save(fixture);
        }

        // Transition and process
        matchday.setStatus(MatchdayStatus.RESULTS_LOADED);
        matchdayRepository.save(matchday);
        processingService.process(matchdayId);

        redirectAttributes.addFlashAttribute("success",
                "Risultati giornata " + matchday.getNumber() + " caricati e schedine elaborate.");
        return "redirect:/admin/leagues/" + leagueId + "/calendar";
    }
}