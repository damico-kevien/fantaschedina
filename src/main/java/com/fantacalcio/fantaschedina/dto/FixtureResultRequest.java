package com.fantacalcio.fantaschedina.dto;

import lombok.Data;

@Data
public class FixtureResultRequest {
    private Long fixtureId;
    private Integer homeScore;
    private Integer awayScore;
}