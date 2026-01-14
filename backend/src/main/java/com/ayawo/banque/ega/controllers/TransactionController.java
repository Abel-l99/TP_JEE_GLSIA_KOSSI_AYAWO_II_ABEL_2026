package com.ayawo.banque.ega.controllers;

import com.ayawo.banque.ega.dto.transaction.TransactionRequestDTO;
import com.ayawo.banque.ega.dto.transaction.TransactionResponseDTO;
import com.ayawo.banque.ega.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * 1. CREATE - Créer une transaction (dépôt, retrait, virement)
     *
     * POST /api/transactions
     * Body: TransactionRequestDTO
     */
    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @Valid @RequestBody TransactionRequestDTO requestDTO) {

        log.info("Requête POST /api/transactions - Création d'une transaction {}", requestDTO.getType());

        TransactionResponseDTO createdTransaction = transactionService.createTransaction(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdTransaction);
    }

    /**
     * 2. READ ALL - Récupérer toutes les transactions
     *
     * GET /api/transactions
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactions() {

        log.info("Requête GET /api/transactions - Récupération de toutes les transactions");

        List<TransactionResponseDTO> transactions = transactionService.getAllTransactions();

        return ResponseEntity.ok(transactions);
    }

    /**
     * 4. READ BY COMPTE - Récupérer les transactions d'un compte
     *
     * GET /api/transactions/compte/{numeroCompte}
     */
    @GetMapping("/compte/{numeroCompte}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByNumeroCompte(
            @PathVariable String numeroCompte) {

        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByNumeroCompte(numeroCompte);

        return ResponseEntity.ok(transactions);
    }

    /**
     * 5. READ BY PERIODE - Récupérer les transactions d'un compte sur une période
     *
     * GET /api/transactions/compte/{numeroCompte}/periode?dateDebut=...&dateFin=...
     */
    @GetMapping("/compte/{numeroCompte}/periode")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByPeriode(
            @PathVariable String numeroCompte,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {

        List<TransactionResponseDTO> transactions = transactionService
                .getTransactionsByNumeroCompteAndPeriode(numeroCompte, dateDebut, dateFin);

        return ResponseEntity.ok(transactions);
    }

    /**
     * 7. READ BY CLIENT - Récupérer toutes les transactions d'un client
     *
     * GET /api/transactions/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByClientId(
            @PathVariable Long clientId) {

        List<TransactionResponseDTO> transactions = transactionService.getTransactionsByClientId(clientId);

        return ResponseEntity.ok(transactions);
    }

    /**
     * 8. READ BY CLIENT AND PERIODE - Récupérer les transactions d'un client sur une période
     *
     * GET /api/transactions/client/{clientId}/periode?dateDebut=...&dateFin=...
     */
    @GetMapping("/client/{clientId}/periode")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByClientIdAndPeriode(
            @PathVariable Long clientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {

        List<TransactionResponseDTO> transactions = transactionService
                .getTransactionsByClientIdAndPeriode(clientId, dateDebut, dateFin);

        return ResponseEntity.ok(transactions);
    }

    /**
     * 6. DELETE - Supprimer une transaction
     *
     * DELETE /api/transactions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(@PathVariable Long id) {

        transactionService.deleteTransaction(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Transaction supprimée avec succès");
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countClients() {

        long count = transactionService.countTransactionsToday();

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }
}