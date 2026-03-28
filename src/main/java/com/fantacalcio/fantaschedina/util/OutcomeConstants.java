package com.fantacalcio.fantaschedina.util;

import com.fantacalcio.fantaschedina.domain.enums.OutcomeType;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class OutcomeConstants {

    private OutcomeConstants() {}

    public static final Map<OutcomeType, List<String>> VALID_OUTCOMES = Map.of(
            OutcomeType.RESULT_1X2,    List.of("1", "X", "2"),
            OutcomeType.DOUBLE_CHANCE, List.of("1X", "12", "X2"),
            OutcomeType.GOAL_NOGOAL,   List.of("GG", "NG"),
            OutcomeType.OVER_UNDER,    List.of("OVER", "UNDER")
    );

    public static final Map<OutcomeType, Set<String>> VALID_OUTCOMES_SET = VALID_OUTCOMES.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> Set.copyOf(e.getValue())));
}