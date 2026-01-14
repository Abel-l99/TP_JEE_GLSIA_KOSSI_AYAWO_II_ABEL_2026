package com.ayawo.banque.ega.controllers;

import com.ayawo.banque.ega.dto.compte.CompteRequestDTO;
import com.ayawo.banque.ega.dto.compte.CompteResponseDTO;
import com.ayawo.banque.ega.dto.compte.CompteSummaryDTO;
import com.ayawo.banque.ega.dto.compte.CompteUpdateDTO;
import com.ayawo.banque.ega.services.CompteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/compte")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CompteController {

    private final CompteService compteService;

    /**
     * 1. CREATE - Créer un nouveau compte
     *
     * POST /compte
     * Body: { "typeCompte": "COURANT", "clientId": 1 }
     */
    @PostMapping
    public ResponseEntity<CompteResponseDTO> createCompte(
            @Valid @RequestBody CompteRequestDTO requestDTO) {

        CompteResponseDTO createdCompte = compteService.createCompte(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdCompte);
    }

    /**
     * 2. READ ALL - Récupérer tous les comptes
     *
     * GET /compte
     */
    @GetMapping
    public ResponseEntity<List<CompteResponseDTO>> getAllComptes() {

        List<CompteResponseDTO> comptes = compteService.getAllComptes();

        return ResponseEntity.ok(comptes);
    }

    /**
     * 3. READ BY NUMERO - Récupérer un compte par son numéro IBAN
     *
     * GET /api/comptes/{numeroCompte}
     */
    @GetMapping("/{numeroCompte}")
    public ResponseEntity<CompteResponseDTO> getCompteByNumero(
            @PathVariable String numeroCompte) {

        CompteResponseDTO compte = compteService.getCompteByNumero(numeroCompte);

        return ResponseEntity.ok(compte);
    }

    /**
     * 4. READ BY CLIENT ID - Récupérer tous les comptes d'un client
     *
     * GET /api/comptes/client/{clientId}
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CompteResponseDTO>> getComptesByClientId(
            @PathVariable Long clientId) {

        List<CompteResponseDTO> comptes = compteService.getComptesByClientId(clientId);

        return ResponseEntity.ok(comptes);
    }

    /**
     * 5. UPDATE - Modifier un compte
     *
     * PUT /api/comptes/{numeroCompte}
     * Body: { "clientId": 2 }
     */
    @PutMapping("/{numeroCompte}")
    public ResponseEntity<CompteResponseDTO> updateCompte(
            @PathVariable String numeroCompte,
            @Valid @RequestBody CompteUpdateDTO updateDTO) {

        CompteResponseDTO updatedCompte = compteService.updateCompte(numeroCompte, updateDTO);

        return ResponseEntity.ok(updatedCompte);
    }

    /**
     * 6. DELETE - Supprimer un compte (uniquement si solde = 0)
     *
     * DELETE /api/comptes/{numeroCompte}
     */
    @DeleteMapping("/{numeroCompte}")
    public ResponseEntity<Map<String, String>> deleteCompte(
            @PathVariable String numeroCompte) {

        compteService.deleteCompte(numeroCompte);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Compte supprimé avec succès");
        response.put("numeroCompte", numeroCompte);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countComptes() {

        long count = compteService.countComptes();

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/total-solde")
    public ResponseEntity<Map<String, BigDecimal>> getTotalSolde() {
        BigDecimal total = compteService.getTotalSolde();

        Map<String, BigDecimal> response = new HashMap<>();
        response.put("total", total);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<List<CompteSummaryDTO>> getAllComptesSummary() {
        List<CompteSummaryDTO> comptes = compteService.getAllComptesSummary();
        return ResponseEntity.ok(comptes);
    }

}