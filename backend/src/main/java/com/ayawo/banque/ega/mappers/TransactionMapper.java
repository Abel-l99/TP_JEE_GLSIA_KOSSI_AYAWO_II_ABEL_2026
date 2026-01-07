package com.ayawo.banque.ega.mappers;

import com.ayawo.banque.ega.dto.transaction.TransactionResponseDTO;
import com.ayawo.banque.ega.entities.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponseDTO toResponseDTO(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        TransactionResponseDTO.TransactionResponseDTOBuilder builder = TransactionResponseDTO.builder()
                .id(entity.getId())
                .type(entity.getType())
                .montant(entity.getMontant())
                .date(entity.getDate())
                .numeroCompteSource(entity.getCompteSource().getNumeroCompte())
                .proprietaireSource(entity.getCompteSource().getProprietaire().getNomComplet());

        // Ajouter les infos du compte destination si c'est un virement
        if (entity.getCompteDestination() != null) {
            builder.numeroCompteDestination(entity.getCompteDestination().getNumeroCompte())
                    .proprietaireDestination(entity.getCompteDestination().getProprietaire().getNomComplet());
        }

        return builder.build();
    }
}