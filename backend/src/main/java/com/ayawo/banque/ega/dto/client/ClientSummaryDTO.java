package com.ayawo.banque.ega.dto.client;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientSummaryDTO {

    private Long id;
    private String nomComplet;  // prenom + nom
    private String email;
    private String telephone;
    private int nombreComptes;
}