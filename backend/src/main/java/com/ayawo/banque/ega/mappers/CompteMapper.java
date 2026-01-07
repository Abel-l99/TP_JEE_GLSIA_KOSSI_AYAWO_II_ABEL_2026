package com.ayawo.banque.ega.mappers;

import com.ayawo.banque.ega.dto.compte.CompteResponseDTO;
import com.ayawo.banque.ega.dto.compte.CompteSummaryDTO;
import com.ayawo.banque.ega.entities.CompteEntity;
import org.springframework.stereotype.Component;

@Component
public class CompteMapper {

    /**
     * Convertit CompteEntity → CompteResponseDTO
     */
    public CompteResponseDTO toResponseDTO(CompteEntity entity) {
        if (entity == null) {
            return null;
        }

        return CompteResponseDTO.builder()
                .numeroCompte(entity.getNumeroCompte())
                .typeCompte(entity.getTypeCompte())
                .dateCreation(entity.getDateCreation())
                .solde(entity.getSolde())
                .proprietaireId(entity.getProprietaire().getId())
                .proprietaireNom(entity.getProprietaire().getNomComplet())
                .proprietaireEmail(entity.getProprietaire().getEmail())
                .nombreTransactions(entity.getTransactions() != null ? entity.getTransactions().size() : 0)
                .build();
    }

    /**
     * Convertit CompteEntity → CompteSummaryDTO
     */
    public CompteSummaryDTO toSummaryDTO(CompteEntity entity) {
        if (entity == null) {
            return null;
        }

        return CompteSummaryDTO.builder()
                .numeroCompte(entity.getNumeroCompte())
                .typeCompte(entity.getTypeCompte())
                .solde(entity.getSolde())
                .proprietaireNom(entity.getProprietaire().getNomComplet())
                .build();
    }
}