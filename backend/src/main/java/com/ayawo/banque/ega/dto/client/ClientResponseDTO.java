package com.ayawo.banque.ega.dto.client;

import com.ayawo.banque.ega.enums.Sexe;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientResponseDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String nomComplet;  // Calculé : prenom + nom
    private LocalDate dateNaissance;
    private Sexe sexe;
    private String adresse;
    private String telephone;
    private String email;
    private String nationalite;
    private int nombreComptes;  // Nombre de comptes du client
}
