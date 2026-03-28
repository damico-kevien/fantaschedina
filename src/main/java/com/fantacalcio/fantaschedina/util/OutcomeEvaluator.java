package com.fantacalcio.fantaschedina.util;

import com.fantacalcio.fantaschedina.domain.enums.OutcomeType;

public class OutcomeEvaluator {

    private OutcomeEvaluator() {}

    public static boolean evaluate(OutcomeType type, String picked,
                                   int homeScore, int awayScore,
                                   Double overUnderThreshold) {
        return switch (type) {
            case RESULT_1X2 -> switch (picked) {
                case "1" -> homeScore > awayScore;
                case "X" -> homeScore == awayScore;
                case "2" -> homeScore < awayScore;
                default  -> false;
            };
            case DOUBLE_CHANCE -> switch (picked) {
                case "1X" -> homeScore >= awayScore;
                case "12" -> homeScore != awayScore;
                case "X2" -> homeScore <= awayScore;
                default   -> false;
            };
            case GOAL_NOGOAL -> switch (picked) {
                case "GG" -> homeScore > 0 && awayScore > 0;
                case "NG" -> homeScore == 0 || awayScore == 0;
                default   -> false;
            };
            case OVER_UNDER -> {
                double threshold = overUnderThreshold != null ? overUnderThreshold : 2.5;
                yield switch (picked) {
                    case "OVER"  -> (homeScore + awayScore) > threshold;
                    case "UNDER" -> (homeScore + awayScore) <= threshold;
                    default      -> false;
                };
            }
        };
    }
}