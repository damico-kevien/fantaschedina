package com.fantacalcio.fantaschedina.dto;

import com.fantacalcio.fantaschedina.domain.enums.OutcomeType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BetTemplateRowRequest {

    @NotNull
    private OutcomeType outcomeType;

    @NotNull
    @Min(0)
    private Integer requiredCount = 0;

    private Double overUnderThreshold = 2.5;
}
