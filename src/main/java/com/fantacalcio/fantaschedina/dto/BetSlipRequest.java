package com.fantacalcio.fantaschedina.dto;

import lombok.Data;

import java.util.List;

@Data
public class BetSlipRequest {
    private List<BetPickRequest> picks;
}