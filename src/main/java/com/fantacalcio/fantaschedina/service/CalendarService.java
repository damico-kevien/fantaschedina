package com.fantacalcio.fantaschedina.service;

import com.fantacalcio.fantaschedina.domain.entity.Matchday;
import com.fantacalcio.fantaschedina.domain.entity.MatchdayFixture;
import com.fantacalcio.fantaschedina.domain.enums.MatchdayStatus;
import com.fantacalcio.fantaschedina.dto.MatchdayScheduleRequest;
import com.fantacalcio.fantaschedina.repository.FantaTeamRepository;
import com.fantacalcio.fantaschedina.repository.LeagueRepository;
import com.fantacalcio.fantaschedina.repository.MatchdayFixtureRepository;
import com.fantacalcio.fantaschedina.repository.MatchdayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CalendarService {

    private final LeagueRepository leagueRepository;
    private final MatchdayRepository matchdayRepository;
    private final MatchdayFixtureRepository matchdayFixtureRepository;
    private final FantaTeamRepository fantaTeamRepository;

    public void importCsv(Long leagueId, MultipartFile file) {
        leagueRepository.findById(leagueId)
            .orElseThrow(() -> new IllegalArgumentException("Lega non trovata"));

        Map<String, Long> teamNameToId = fantaTeamRepository.findByLeagueId(leagueId).stream()
            .collect(Collectors.toMap(t -> t.getName(), t -> t.getId()));

        Map<Integer, List<String[]>> grouped = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String header = reader.readLine();
            if (header == null || !header.trim().equals("matchday_number,home_team,away_team")) {
                throw new IllegalArgumentException(
                    "Intestazione CSV non valida. Attesa: matchday_number,home_team,away_team");
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length != 3) {
                    errors.add("Riga " + lineNum + ": formato non valido (attese 3 colonne)");
                    continue;
                }
                try {
                    int number = Integer.parseInt(parts[0].trim());
                    grouped.computeIfAbsent(number, k -> new ArrayList<>()).add(parts);
                } catch (NumberFormatException e) {
                    errors.add("Riga " + lineNum + ": numero giornata non valido");
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Errore nella lettura del file CSV");
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Errori nel CSV: " + String.join("; ", errors));
        }

        // Validate team names
        Set<String> unknown = new LinkedHashSet<>();
        for (List<String[]> rows : grouped.values()) {
            for (String[] row : rows) {
                String home = row[1].trim();
                String away = row[2].trim();
                if (!teamNameToId.containsKey(home)) unknown.add(home);
                if (!teamNameToId.containsKey(away)) unknown.add(away);
            }
        }
        if (!unknown.isEmpty()) {
            throw new IllegalArgumentException(
                "Squadre non trovate nella lega: " + String.join(", ", unknown));
        }

        // Check for matchdays that already have fixtures
        List<Integer> existing = new ArrayList<>();
        for (Integer number : grouped.keySet()) {
            matchdayRepository.findByLeagueIdAndNumber(leagueId, number).ifPresent(md -> {
                if (matchdayFixtureRepository.countByMatchdayId(md.getId()) > 0) {
                    existing.add(number);
                }
            });
        }
        if (!existing.isEmpty()) {
            throw new IllegalArgumentException(
                "Giornate con partite già caricate: " + existing + ". Rimuovere prima le partite esistenti.");
        }

        // Persist
        for (Map.Entry<Integer, List<String[]>> entry : grouped.entrySet()) {
            Integer number = entry.getKey();
            Matchday matchday = matchdayRepository.findByLeagueIdAndNumber(leagueId, number)
                .orElseGet(() -> matchdayRepository.save(Matchday.builder()
                    .leagueId(leagueId)
                    .number(number)
                    .status(MatchdayStatus.SCHEDULED)
                    .build()));

            for (String[] row : entry.getValue()) {
                matchdayFixtureRepository.save(MatchdayFixture.builder()
                    .matchdayId(matchday.getId())
                    .homeFantaTeamId(teamNameToId.get(row[1].trim()))
                    .awayFantaTeamId(teamNameToId.get(row[2].trim()))
                    .build());
            }
        }
    }

    public void scheduleMatchday(Long matchdayId, MatchdayScheduleRequest request) {
        Matchday matchday = matchdayRepository.findById(matchdayId)
            .orElseThrow(() -> new IllegalArgumentException("Giornata non trovata"));

        if (matchday.getStatus() != MatchdayStatus.SCHEDULED) {
            throw new IllegalStateException(
                "Le date possono essere modificate solo su giornate in stato SCHEDULED");
        }

        matchday.setStartAt(request.getStartAt());
        matchday.setEndAt(request.getEndAt());
        matchday.setDeadlineOverride(request.getDeadlineOverride());
        matchdayRepository.save(matchday);
    }

    public Matchday addMatchday(Long leagueId, Integer number) {
        leagueRepository.findById(leagueId)
            .orElseThrow(() -> new IllegalArgumentException("Lega non trovata"));
        if (matchdayRepository.findByLeagueIdAndNumber(leagueId, number).isPresent()) {
            throw new IllegalArgumentException("Giornata " + number + " già esistente");
        }
        return matchdayRepository.save(Matchday.builder()
            .leagueId(leagueId)
            .number(number)
            .status(MatchdayStatus.SCHEDULED)
            .build());
    }

    public void addFixture(Long leagueId, Long matchdayId, Long homeTeamId, Long awayTeamId) {
        Matchday matchday = matchdayRepository.findById(matchdayId)
            .orElseThrow(() -> new IllegalArgumentException("Giornata non trovata"));
        if (!matchday.getLeagueId().equals(leagueId)) {
            throw new IllegalArgumentException("La giornata non appartiene a questa lega");
        }
        if (matchday.getStatus() != MatchdayStatus.SCHEDULED) {
            throw new IllegalStateException("Impossibile aggiungere partite a una giornata non in stato SCHEDULED");
        }
        if (homeTeamId.equals(awayTeamId)) {
            throw new IllegalArgumentException("La squadra di casa e quella in trasferta devono essere diverse");
        }
        fantaTeamRepository.findById(homeTeamId)
            .filter(t -> t.getLeagueId().equals(leagueId))
            .orElseThrow(() -> new IllegalArgumentException("Squadra di casa non valida"));
        fantaTeamRepository.findById(awayTeamId)
            .filter(t -> t.getLeagueId().equals(leagueId))
            .orElseThrow(() -> new IllegalArgumentException("Squadra in trasferta non valida"));

        matchdayFixtureRepository.save(MatchdayFixture.builder()
            .matchdayId(matchdayId)
            .homeFantaTeamId(homeTeamId)
            .awayFantaTeamId(awayTeamId)
            .build());
    }

    @Transactional(readOnly = true)
    public List<Matchday> findMatchdaysByLeague(Long leagueId) {
        return matchdayRepository.findByLeagueIdOrderByNumberAsc(leagueId);
    }

    @Transactional(readOnly = true)
    public Map<Long, List<MatchdayFixture>> findFixturesGroupedByMatchday(List<Matchday> matchdays) {
        Map<Long, List<MatchdayFixture>> result = new LinkedHashMap<>();
        for (Matchday md : matchdays) {
            result.put(md.getId(), matchdayFixtureRepository.findByMatchdayId(md.getId()));
        }
        return result;
    }
}