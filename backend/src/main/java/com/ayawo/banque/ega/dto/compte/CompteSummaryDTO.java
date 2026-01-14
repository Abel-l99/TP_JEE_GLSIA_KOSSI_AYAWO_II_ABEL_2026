package com.ayawo.banque.ega.dto.compte;

import com.ayawo.banque.ega.enums.TypeCompte;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteSummaryDTO {

    private String numeroCompte;
    private TypeCompte typeCompte;
    private BigDecimal solde;
    private LocalDateTime dateCreation;
    private String proprietaireNom;
}