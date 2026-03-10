package com.fantacalcio.fantaschedina.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BetTemplateForm {

    private List<BetTemplateRowRequest> rows = new ArrayList<>();
}
