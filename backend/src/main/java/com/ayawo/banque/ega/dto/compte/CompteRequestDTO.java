package com.ayawo.banque.ega.dto.compte;

import com.ayawo.banque.ega.enums.TypeCompte;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteRequestDTO {

    @NotNull(message = "Le type de compte est obligatoire")
    private TypeCompte typeCompte;

    @NotNull(message = "L'ID du client propriétaire est obligatoire")
    private Long clientId;
}
