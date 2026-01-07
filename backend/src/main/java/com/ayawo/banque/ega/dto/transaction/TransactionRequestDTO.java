package com.ayawo.banque.ega.dto.transaction;

import com.ayawo.banque.ega.enums.TypeTransaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequestDTO {

    @NotNull(message = "Le type de transaction est obligatoire")
    private TypeTransaction type;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à 0")
    private BigDecimal montant;

    @NotBlank(message = "Le numéro du compte source est obligatoire")
    private String numeroCompteSource;

    // Obligatoire uniquement pour les virements
    private String numeroCompteDestination;

    private String description;
}