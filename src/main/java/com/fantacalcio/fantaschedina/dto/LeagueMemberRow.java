package com.fantacalcio.fantaschedina.dto;

import com.fantacalcio.fantaschedina.domain.entity.FantaTeam;
import com.fantacalcio.fantaschedina.domain.entity.LeagueMembership;
import com.fantacalcio.fantaschedina.domain.entity.User;

public record LeagueMemberRow(
        LeagueMembership membership,
        User user,
        FantaTeam fantaTeam
) {}