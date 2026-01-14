package com.ayawo.banque.ega.controllers;

import com.ayawo.banque.ega.dto.client.ClientRequestDTO;
import com.ayawo.banque.ega.dto.client.ClientResponseDTO;
import com.ayawo.banque.ega.dto.client.ClientSummaryDTO;
import com.ayawo.banque.ega.services.ClientService;
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
@RequestMapping("/client")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClientController {

    private final ClientService clientService;

    /**
     * CREATE - Créer un nouveau client
     *
     * POST /api/clients
     * Body: ClientRequestDTO
     *
     * @param requestDTO Les données du client à créer
     * @return Le client créé avec code 201 (CREATED)
     */
    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(
            @Valid @RequestBody ClientRequestDTO requestDTO) {

        ClientResponseDTO createdClient = clientService.createClient(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdClient);
    }

    /**
     * READ - Récupérer tous les clients (version complète)
     *
     * GET /api/clients
     *
     * @return Liste de tous les clients avec code 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> getAllClients() {

        List<ClientResponseDTO> clients = clientService.getAllClients();

        return ResponseEntity.ok(clients);
    }

    /**
     * READ - Récupérer tous les clients (version résumée)
     *
     * GET /api/clients/summary
     *
     * @return Liste résumée de tous les clients avec code 200 (OK)
     */
    @GetMapping("/summary")
    public ResponseEntity<List<ClientSummaryDTO>> getAllClientsSummary() {

        List<ClientSummaryDTO> clients = clientService.getAllClientsSummary();

        return ResponseEntity.ok(clients);
    }

    /**
     * READ - Récupérer un client par son ID
     *
     * GET /api/clients/{id}
     *
     * @param id L'identifiant du client
     * @return Le client trouvé avec code 200 (OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable Long id) {

        ClientResponseDTO client = clientService.getClientById(id);

        return ResponseEntity.ok(client);
    }

    /**
     * READ - Récupérer un client par son email
     *
     * GET /api/clients/email/{email}
     *
     * @param email L'email du client
     * @return Le client trouvé avec code 200 (OK)
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ClientResponseDTO> getClientByEmail(@PathVariable String email) {

        ClientResponseDTO client = clientService.getClientByEmail(email);

        return ResponseEntity.ok(client);
    }

    /**
     * UPDATE - Mettre à jour un client existant
     *
     * PUT /api/clients/{id}
     * Body: ClientRequestDTO
     *
     * @param id L'identifiant du client à modifier
     * @param requestDTO Les nouvelles données
     * @return Le client mis à jour avec code 200 (OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequestDTO requestDTO) {

        ClientResponseDTO updatedClient = clientService.updateClient(id, requestDTO);

        return ResponseEntity.ok(updatedClient);
    }

    /**
     * DELETE - Supprimer un client
     *
     * DELETE /clients/{id}
     *
     * @param id L'identifiant du client à supprimer
     * @return Message de confirmation avec code 200 (OK)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteClient(@PathVariable Long id) {

        clientService.deleteClient(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Client supprimé avec succès");
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Compter le nombre total de clients
     *
     * GET /api/clients/count
     *
     * @return Le nombre de clients
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countClients() {

        long count = clientService.countClients();

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Vérifier si un email est disponible
     *
     * GET /api/clients/check-email?email=xxx@xxx.com
     *
     * @param email L'email à vérifier
     * @return true si disponible, false sinon
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmailAvailability(
            @RequestParam String email) {

        boolean available = clientService.isEmailAvailable(email);

        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);

        return ResponseEntity.ok(response);  // 200
    }

}