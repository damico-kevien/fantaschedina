package com.fantacalcio.fantaschedina.dto;

import com.fantacalcio.fantaschedina.domain.enums.OutcomeType;
import lombok.Data;

@Data
public class BetPickRequest {
    private Long fixtureId;
    private OutcomeType outcomeType;
    private String pickedOutcome;
}