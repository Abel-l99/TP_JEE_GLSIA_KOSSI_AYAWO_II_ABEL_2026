package com.ayawo.banque.ega.mappers;

import com.ayawo.banque.ega.dto.client.ClientRequestDTO;
import com.ayawo.banque.ega.dto.client.ClientResponseDTO;
import com.ayawo.banque.ega.dto.client.ClientSummaryDTO;
import com.ayawo.banque.ega.entities.ClientEntity;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    /**
     * Convertit ClientRequestDTO → ClientEntity
     * Utilisé lors de la création d'un client
     */
    public ClientEntity toEntity(ClientRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        return ClientEntity.builder()
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .dateNaissance(dto.getDateNaissance())
                .sexe(dto.getSexe())
                .adresse(dto.getAdresse())
                .telephone(dto.getTelephone())
                .email(dto.getEmail())
                .nationalite(dto.getNationalite())
                .build();
    }

    /**
     * Met à jour une entité existante avec les données du DTO
     * Utilisé lors de la modification
     */
    public void updateEntity(ClientEntity entity, ClientRequestDTO dto) {
        if (entity == null || dto == null) {
            return;
        }

        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        entity.setDateNaissance(dto.getDateNaissance());
        entity.setSexe(dto.getSexe());
        entity.setAdresse(dto.getAdresse());
        entity.setTelephone(dto.getTelephone());
        entity.setEmail(dto.getEmail());
        entity.setNationalite(dto.getNationalite());
    }

    /**
     * Convertit ClientEntity → ClientResponseDTO
     * Utilisé pour renvoyer les détails d'un client
     */
    public ClientResponseDTO toResponseDTO(ClientEntity entity) {
        if (entity == null) {
            return null;
        }

        return ClientResponseDTO.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .prenom(entity.getPrenom())
                .nomComplet(entity.getNomComplet())
                .dateNaissance(entity.getDateNaissance())
                .sexe(entity.getSexe())
                .adresse(entity.getAdresse())
                .telephone(entity.getTelephone())
                .email(entity.getEmail())
                .nationalite(entity.getNationalite())
                .nombreComptes(entity.getComptes() != null ? entity.getComptes().size() : 0)
                .build();
    }

    /**
     * Convertit ClientEntity → ClientSummaryDTO
     * Utilisé pour les listes (version allégée)
     */
    public ClientSummaryDTO toSummaryDTO(ClientEntity entity) {
        if (entity == null) {
            return null;
        }

        return ClientSummaryDTO.builder()
                .id(entity.getId())
                .nomComplet(entity.getNomComplet())
                .email(entity.getEmail())
                .telephone(entity.getTelephone())
                .nombreComptes(entity.getComptes() != null ? entity.getComptes().size() : 0)
                .build();
    }
}