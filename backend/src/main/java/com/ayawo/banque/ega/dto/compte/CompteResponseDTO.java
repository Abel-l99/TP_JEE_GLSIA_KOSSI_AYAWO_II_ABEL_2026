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
public class CompteResponseDTO {

    private String numeroCompte;
    private TypeCompte typeCompte;
    private LocalDateTime dateCreation;
    private BigDecimal solde;

    // Informations du propriétaire
    private Long proprietaireId;
    private String proprietaireNom;
    private String proprietaireEmail;

    private int nombreTransactions;
}
