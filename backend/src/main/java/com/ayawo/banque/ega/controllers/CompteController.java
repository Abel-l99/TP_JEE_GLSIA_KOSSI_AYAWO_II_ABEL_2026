package com.ayawo.banque.ega.controllers;

import com.ayawo.banque.ega.dto.compte.CompteRequestDTO;
import com.ayawo.banque.ega.dto.compte.CompteResponseDTO;
import com.ayawo.banque.ega.dto.compte.CompteUpdateDTO;
import com.ayawo.banque.ega.services.CompteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * POST /api/comptes
     * Body: { "typeCompte": "COURANT", "clientId": 1 }
     */
    @PostMapping
    public ResponseEntity<CompteResponseDTO> createCompte(
            @Valid @RequestBody CompteRequestDTO requestDTO) {

        log.info("Requête POST /api/comptes - Création d'un compte");

        CompteResponseDTO createdCompte = compteService.createCompte(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdCompte);
    }

    /**
     * 2. READ ALL - Récupérer tous les comptes
     *
     * GET /api/comptes
     */
    @GetMapping
    public ResponseEntity<List<CompteResponseDTO>> getAllComptes() {

        log.info("Requête GET /api/comptes - Récupération de tous les comptes");

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

        log.info("Requête GET /api/comptes/{} - Récupération du compte", numeroCompte);

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

        log.info("Requête GET /api/comptes/client/{} - Récupération des comptes du client", clientId);

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

        log.info("Requête PUT /api/comptes/{} - Mise à jour du compte", numeroCompte);

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

        log.info("Requête DELETE /api/comptes/{} - Suppression du compte", numeroCompte);

        compteService.deleteCompte(numeroCompte);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Compte supprimé avec succès");
        response.put("numeroCompte", numeroCompte);

        return ResponseEntity.ok(response);
    }
}