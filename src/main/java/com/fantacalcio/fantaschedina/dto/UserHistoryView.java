package com.fantacalcio.fantaschedina.dto;

import com.fantacalcio.fantaschedina.domain.entity.*;

import java.util.List;
import java.util.Map;

public record UserHistoryView(
        LeagueMembership membership,
        FantaTeam myTeam,
        List<BetSlip> slips,
        Map<Long, Matchday> matchdayMap,
        List<CreditTransaction> transactions
) {}