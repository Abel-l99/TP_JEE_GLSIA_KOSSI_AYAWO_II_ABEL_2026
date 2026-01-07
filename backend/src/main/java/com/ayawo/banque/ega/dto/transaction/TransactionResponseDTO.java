package com.ayawo.banque.ega.dto.transaction;

import com.ayawo.banque.ega.enums.TypeTransaction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {

    private Long id;
    private TypeTransaction type;
    private BigDecimal montant;
    private LocalDateTime date;
    private String description;

    // Compte source
    private String numeroCompteSource;
    private String proprietaireSource;

    // Compte destination (pour virements)
    private String numeroCompteDestination;
    private String proprietaireDestination;
}