package com.ayawo.banque.ega.services;

import com.ayawo.banque.ega.dto.transaction.TransactionRequestDTO;
import com.ayawo.banque.ega.dto.transaction.TransactionResponseDTO;
import com.ayawo.banque.ega.entities.CompteEntity;
import com.ayawo.banque.ega.entities.TransactionEntity;
import com.ayawo.banque.ega.enums.TypeTransaction;
import com.ayawo.banque.ega.exceptions.client.ClientNotFoundException;
import com.ayawo.banque.ega.exceptions.compte.CompteNotFoundException;
import com.ayawo.banque.ega.exceptions.transaction.SoldeInsuffisantException;
import com.ayawo.banque.ega.exceptions.transaction.TransactionNotFoundException;
import com.ayawo.banque.ega.mappers.TransactionMapper;
import com.ayawo.banque.ega.repositories.ClientRepository;
import com.ayawo.banque.ega.repositories.CompteRepository;
import com.ayawo.banque.ega.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CompteRepository compteRepository;
    private final TransactionMapper transactionMapper;
    private final ClientRepository clientRepository;

    /**
     * 1. CREATE - Créer une transaction (dépôt, retrait, virement)
     */
    public TransactionResponseDTO createTransaction(TransactionRequestDTO requestDTO) {
        log.info("Création d'une transaction de type : {}", requestDTO.getType());

        // Récupérer le compte source
        CompteEntity compteSource = compteRepository.findByNumeroCompte(requestDTO.getNumeroCompteSource())
                .orElseThrow(() -> {
                    log.error("Compte source non trouvé : {}", requestDTO.getNumeroCompteSource());
                    return new CompteNotFoundException(requestDTO.getNumeroCompteSource());
                });

        TransactionEntity transaction;

        // Exécuter la transaction selon son type
        switch (requestDTO.getType()) {
            case DEPOT:
                transaction = effectuerDepot(compteSource, requestDTO);
                break;

            case RETRAIT:
                transaction = effectuerRetrait(compteSource, requestDTO);
                break;

            case VIREMENT:
                transaction = effectuerVirement(compteSource, requestDTO);
                break;

            default:
                throw new IllegalArgumentException("Type de transaction invalide : " + requestDTO.getType());
        }

        // Sauvegarder la transaction
        TransactionEntity savedTransaction = transactionRepository.save(transaction);

        log.info("Transaction créée avec succès. ID : {} - Nouveau solde du compte source : {}",
                savedTransaction.getId(), compteSource.getSolde());

        return transactionMapper.toResponseDTO(savedTransaction);
    }

    /**
     * 2. READ ALL - Récupérer toutes les transactions
     */
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getAllTransactions() {
        log.info("Récupération de toutes les transactions");

        return transactionRepository.findAll()
                .stream()
                .map(transactionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 3. READ BY ID - Récupérer une transaction par son ID
     */
    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransactionById(Long id) {
        log.info("Recherche de la transaction ID: {}", id);

        TransactionEntity transaction = transactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Transaction non trouvée avec l'ID : {}", id);
                    return new TransactionNotFoundException(id);
                });

        return transactionMapper.toResponseDTO(transaction);
    }

    /**
     * 4. READ BY COMPTE - Récupérer les transactions d'un compte
     */
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByNumeroCompte(String numeroCompte) {
        log.info("Récupération des transactions du compte : {}", numeroCompte);

        // Vérifier que le compte existe
        if (!compteRepository.existsByNumeroCompte(numeroCompte)) {
            log.error("Compte non trouvé : {}", numeroCompte);
            throw new CompteNotFoundException(numeroCompte);
        }

        return transactionRepository.findByNumeroCompte(numeroCompte)
                .stream()
                .map(transactionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 5. READ BY PERIODE - Récupérer les transactions d'un compte sur une période
     */
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByNumeroCompteAndPeriode(
            String numeroCompte,
            LocalDateTime dateDebut,
            LocalDateTime dateFin) {

        log.info("Récupération des transactions du compte {} entre {} et {}",
                numeroCompte, dateDebut, dateFin);

        // Vérifier que le compte existe
        if (!compteRepository.existsByNumeroCompte(numeroCompte)) {
            throw new CompteNotFoundException(numeroCompte);
        }

        return transactionRepository.findByNumeroCompteAndDateBetween(numeroCompte, dateDebut, dateFin)
                .stream()
                .map(transactionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 6. DELETE - Supprimer une transaction
     */
    public void deleteTransaction(Long id) {
        log.info("Suppression de la transaction ID: {}", id);

        TransactionEntity transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        // Annuler les effets de la transaction avant de la supprimer
        annulerTransaction(transaction);

        transactionRepository.delete(transaction);

        log.info("Transaction supprimée avec succès");
    }

    // ==================== MÉTHODES PRIVÉES ====================

    /**
     * Effectuer un dépôt
     */
    private TransactionEntity effectuerDepot(CompteEntity compte, TransactionRequestDTO requestDTO) {
        log.info("Dépôt de {} sur le compte {}", requestDTO.getMontant(), compte.getNumeroCompte());

        // Créditer le compte
        compte.crediter(requestDTO.getMontant());
        compteRepository.save(compte);

        return TransactionEntity.builder()
                .type(TypeTransaction.DEPOT)
                .montant(requestDTO.getMontant())
                .compteSource(compte)
                .build();
    }

    /**
     * Effectuer un retrait
     */
    private TransactionEntity effectuerRetrait(CompteEntity compte, TransactionRequestDTO requestDTO) {
        log.info("Retrait de {} du compte {}", requestDTO.getMontant(), compte.getNumeroCompte());

        // Vérifier le solde
        if (!compte.soldeEstSuffisant(requestDTO.getMontant())) {
            log.error("Solde insuffisant pour le retrait. Solde: {}, Montant demandé: {}",
                    compte.getSolde(), requestDTO.getMontant());
            throw new SoldeInsuffisantException(compte.getSolde(), requestDTO.getMontant());
        }

        // Débiter le compte
        compte.debiter(requestDTO.getMontant());
        compteRepository.save(compte);

        return TransactionEntity.builder()
                .type(TypeTransaction.RETRAIT)
                .montant(requestDTO.getMontant())
                .compteSource(compte)
                .build();
    }

    /**
     * Effectuer un virement
     */
    private TransactionEntity effectuerVirement(CompteEntity compteSource, TransactionRequestDTO requestDTO) {
        log.info("Virement de {} du compte {} vers {}",
                requestDTO.getMontant(),
                compteSource.getNumeroCompte(),
                requestDTO.getNumeroCompteDestination());

        // Vérifier que le compte destination est fourni
        if (requestDTO.getNumeroCompteDestination() == null || requestDTO.getNumeroCompteDestination().isBlank()) {
            throw new IllegalArgumentException("Le numéro du compte destination est obligatoire pour un virement");
        }

        // Vérifier qu'on ne vire pas vers le même compte
        if (compteSource.getNumeroCompte().equals(requestDTO.getNumeroCompteDestination())) {
            throw new IllegalArgumentException("Impossible de faire un virement vers le même compte");
        }

        // Récupérer le compte destination
        CompteEntity compteDestination = compteRepository.findByNumeroCompte(requestDTO.getNumeroCompteDestination())
                .orElseThrow(() -> {
                    log.error("Compte destination non trouvé : {}", requestDTO.getNumeroCompteDestination());
                    return new CompteNotFoundException(requestDTO.getNumeroCompteDestination());
                });

        // Vérifier le solde du compte source
        if (!compteSource.soldeEstSuffisant(requestDTO.getMontant())) {
            log.error("Solde insuffisant pour le virement. Solde: {}, Montant demandé: {}",
                    compteSource.getSolde(), requestDTO.getMontant());
            throw new SoldeInsuffisantException(compteSource.getSolde(), requestDTO.getMontant());
        }

        // Débiter le compte source
        compteSource.debiter(requestDTO.getMontant());
        compteRepository.save(compteSource);

        // Créditer le compte destination
        compteDestination.crediter(requestDTO.getMontant());
        compteRepository.save(compteDestination);

        return TransactionEntity.builder()
                .type(TypeTransaction.VIREMENT)
                .montant(requestDTO.getMontant())
                .compteSource(compteSource)
                .compteDestination(compteDestination)
                .build();
    }

    /**
     * Annuler une transaction (inverser ses effets)
     */
    private void annulerTransaction(TransactionEntity transaction) {
        log.info("Annulation de la transaction ID: {} de type: {}", transaction.getId(), transaction.getType());

        switch (transaction.getType()) {
            case DEPOT:
                // Inverser le dépôt : débiter le compte
                transaction.getCompteSource().debiter(transaction.getMontant());
                compteRepository.save(transaction.getCompteSource());
                break;

            case RETRAIT:
                // Inverser le retrait : créditer le compte
                transaction.getCompteSource().crediter(transaction.getMontant());
                compteRepository.save(transaction.getCompteSource());
                break;

            case VIREMENT:
                // Inverser le virement
                transaction.getCompteSource().crediter(transaction.getMontant());
                transaction.getCompteDestination().debiter(transaction.getMontant());
                compteRepository.save(transaction.getCompteSource());
                compteRepository.save(transaction.getCompteDestination());
                break;
        }

        log.info("Transaction annulée avec succès");
    }

    /**
     * 7. READ BY CLIENT - Récupérer toutes les transactions d'un client
     */
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByClientId(Long clientId) {
        log.info("Récupération des transactions du client ID: {}", clientId);

        // Vérifier que le client existe
        if (!clientRepository.existsById(clientId)) {
            log.error("Client non trouvé avec l'ID : {}", clientId);
            throw new ClientNotFoundException(clientId);
        }

        return transactionRepository.findByClientId(clientId)
                .stream()
                .map(transactionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 8. READ BY CLIENT AND PERIODE - Récupérer les transactions d'un client sur une période
     */
    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getTransactionsByClientIdAndPeriode(
            Long clientId,
            LocalDateTime dateDebut,
            LocalDateTime dateFin) {

        log.info("Récupération des transactions du client ID {} entre {} et {}",
                clientId, dateDebut, dateFin);

        // Vérifier que le client existe
        if (!clientRepository.existsById(clientId)) {
            throw new ClientNotFoundException(clientId);
        }

        return transactionRepository.findByClientIdAndDateBetween(clientId, dateDebut, dateFin)
                .stream()
                .map(transactionMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countTransactionsToday() {
        LocalDate today = LocalDate.now();

        return transactionRepository.findAll().stream()
                .filter(transaction -> transaction.getDate().toLocalDate().equals(today))
                .count();
    }

}