package com.fantacalcio.fantaschedina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Lo username è obbligatorio")
    @Size(min = 3, max = 50, message = "Lo username deve essere tra 3 e 50 caratteri")
    private String username;

    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 8, message = "La password deve essere di almeno 8 caratteri")
    private String password;

    @NotBlank(message = "Il nome del FantaTeam è obbligatorio")
    @Size(min = 2, max = 100, message = "Il nome del team deve essere tra 2 e 100 caratteri")
    private String fantaTeamName;
}
