package com.ayawo.banque.ega.dto.compte;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteUpdateDTO {

    // Pour permettre le transfert de propriété d'un compte
    private Long clientId;

    // Vous pouvez ajouter d'autres champs modifiables ici si nécessaire
    // Exemple: statut du compte (actif/inactif), limite de découvert, etc.
}