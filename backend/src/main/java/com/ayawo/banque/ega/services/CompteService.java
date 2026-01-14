package com.ayawo.banque.ega.services;

import com.ayawo.banque.ega.dto.compte.CompteRequestDTO;
import com.ayawo.banque.ega.dto.compte.CompteResponseDTO;
import com.ayawo.banque.ega.dto.compte.CompteSummaryDTO;
import com.ayawo.banque.ega.dto.compte.CompteUpdateDTO;
import com.ayawo.banque.ega.entities.ClientEntity;
import com.ayawo.banque.ega.entities.CompteEntity;
import com.ayawo.banque.ega.exceptions.client.ClientNotFoundException;
import com.ayawo.banque.ega.exceptions.compte.CompteNotFoundException;
import com.ayawo.banque.ega.mappers.CompteMapper;
import com.ayawo.banque.ega.repositories.ClientRepository;
import com.ayawo.banque.ega.repositories.CompteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompteService {

    private final CompteRepository compteRepository;
    private final ClientRepository clientRepository;
    private final CompteMapper compteMapper;
    private final IbanGeneratorService ibanGeneratorService;

    /**
     * 1. CREATE - Créer un nouveau compte pour un client
     */
    public CompteResponseDTO createCompte(CompteRequestDTO requestDTO) {
        log.info("Création d'un nouveau compte {} pour le client ID: {}",
                requestDTO.getTypeCompte(), requestDTO.getClientId());

        // Vérifier que le client existe
        ClientEntity client = clientRepository.findById(requestDTO.getClientId())
                .orElseThrow(() -> {
                    log.error("Client non trouvé avec l'ID : {}", requestDTO.getClientId());
                    return new ClientNotFoundException(requestDTO.getClientId());
                });

        // Générer un IBAN unique
        String iban = generateUniqueIban();

        // Créer le compte
        CompteEntity compte = CompteEntity.builder()
                .numeroCompte(iban)
                .typeCompte(requestDTO.getTypeCompte())
                .solde(BigDecimal.ZERO)
                .proprietaire(client)
                .build();

        // Sauvegarder
        CompteEntity savedCompte = compteRepository.save(compte);

        log.info("Compte créé avec succès. IBAN : {}", savedCompte.getNumeroCompte());

        return compteMapper.toResponseDTO(savedCompte);
    }

    /**
     * 2. READ ALL - Récupérer tous les comptes
     */
    @Transactional(readOnly = true)
    public List<CompteResponseDTO> getAllComptes() {
        log.info("Récupération de tous les comptes");

        return compteRepository.findAll()
                .stream()
                .map(compteMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 3. READ BY NUMERO - Récupérer un compte par son numéro IBAN
     */
    @Transactional(readOnly = true)
    public CompteResponseDTO getCompteByNumero(String numeroCompte) {
        log.info("Recherche du compte : {}", numeroCompte);

        CompteEntity compte = compteRepository.findByNumeroCompte(numeroCompte)
                .orElseThrow(() -> {
                    log.error("Compte non trouvé : {}", numeroCompte);
                    return new CompteNotFoundException(numeroCompte);
                });

        return compteMapper.toResponseDTO(compte);
    }

    /**
     * 4. READ BY CLIENT ID - Récupérer tous les comptes d'un client
     */
    @Transactional(readOnly = true)
    public List<CompteResponseDTO> getComptesByClientId(Long clientId) {
        log.info("Récupération des comptes du client ID: {}", clientId);

        // Vérifier que le client existe
        if (!clientRepository.existsById(clientId)) {
            log.error("Client non trouvé avec l'ID : {}", clientId);
            throw new ClientNotFoundException(clientId);
        }

        return compteRepository.findByProprietaireId(clientId)
                .stream()
                .map(compteMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 5. UPDATE - Modifier un compte
     * Note: On peut modifier uniquement certains champs (pas le numéro IBAN ni le type)
     */
    public CompteResponseDTO updateCompte(String numeroCompte, CompteUpdateDTO updateDTO) {
        log.info("Mise à jour du compte : {}", numeroCompte);

        // Récupérer le compte existant
        CompteEntity compte = compteRepository.findByNumeroCompte(numeroCompte)
                .orElseThrow(() -> new CompteNotFoundException(numeroCompte));

        // Mettre à jour uniquement les champs modifiables
        // Note: Le solde ne doit être modifié que via les transactions
        // Ici on peut imaginer modifier d'autres attributs futurs si nécessaire

        // Pour l'instant, on ne modifie rien car :
        // - numeroCompte : immutable (c'est la clé)
        // - typeCompte : immutable (défini à la création)
        // - solde : modifié via transactions uniquement
        // - dateCreation : immutable
        // - proprietaire : ne devrait pas changer

        // Si vous voulez permettre le changement de propriétaire :
        if (updateDTO.getClientId() != null && !updateDTO.getClientId().equals(compte.getProprietaire().getId())) {
            ClientEntity newProprietaire = clientRepository.findById(updateDTO.getClientId())
                    .orElseThrow(() -> new ClientNotFoundException(updateDTO.getClientId()));
            compte.setProprietaire(newProprietaire);
        }

        CompteEntity updatedCompte = compteRepository.save(compte);

        log.info("Compte mis à jour avec succès");

        return compteMapper.toResponseDTO(updatedCompte);
    }

    /**
     * 6. DELETE - Supprimer un compte (uniquement si solde = 0)
     */
    public void deleteCompte(String numeroCompte) {
        log.info("Suppression du compte : {}", numeroCompte);

        CompteEntity compte = compteRepository.findByNumeroCompte(numeroCompte)
                .orElseThrow(() -> new CompteNotFoundException(numeroCompte));

        // Vérifier que le solde est à zéro
        if (compte.getSolde().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "Impossible de supprimer un compte avec un solde non nul. Solde actuel : " + compte.getSolde()
            );
        }

        compteRepository.delete(compte);

        log.info("Compte supprimé avec succès : {}", numeroCompte);
    }

    // ==================== MÉTHODE PRIVÉE ====================

    /**
     * Générer un IBAN unique (qui n'existe pas déjà)
     */
    private String generateUniqueIban() {
        String iban;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            iban = ibanGeneratorService.generateIban();
            attempts++;

            if (attempts >= maxAttempts) {
                throw new RuntimeException("Impossible de générer un IBAN unique après " + maxAttempts + " tentatives");
            }

        } while (compteRepository.existsByNumeroCompte(iban));

        return iban;
    }

    @Transactional(readOnly = true)
    public long countComptes() {
        long count = compteRepository.count();
        return count;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalSolde() {
        return compteRepository.findAll().stream()
                .map(CompteEntity::getSolde)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<CompteSummaryDTO> getAllComptesSummary() {
        return compteRepository.findAll().stream()
                .map(compte -> {
                    ClientEntity client = compte.getProprietaire();
                    String nomComplet = client.getPrenom() + " " + client.getNom();

                    return CompteSummaryDTO.builder()
                            .numeroCompte(compte.getNumeroCompte())
                            .typeCompte(compte.getTypeCompte())
                            .solde(compte.getSolde())
                            .proprietaireNom(nomComplet)
                            .dateCreation(compte.getDateCreation())
                            .build();
                })
                .collect(Collectors.toList());
    }
}