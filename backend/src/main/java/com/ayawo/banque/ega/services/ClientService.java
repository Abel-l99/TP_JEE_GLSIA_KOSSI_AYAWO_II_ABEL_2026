package com.ayawo.banque.ega.services;

import com.ayawo.banque.ega.dto.client.ClientRequestDTO;
import com.ayawo.banque.ega.dto.client.ClientResponseDTO;
import com.ayawo.banque.ega.dto.client.ClientSummaryDTO;
import com.ayawo.banque.ega.entities.ClientEntity;
import com.ayawo.banque.ega.exceptions.client.ClientNotFoundException;
import com.ayawo.banque.ega.exceptions.client.EmailAlreadyExistsException;
import com.ayawo.banque.ega.mappers.ClientMapper;
import com.ayawo.banque.ega.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor  // Lombok génère le constructeur avec les champs final
@Slf4j  // Lombok pour les logs
@Transactional  // Toutes les méthodes sont transactionnelles par défaut
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    /**
     * Créer un nouveau client
     *
     * @param requestDTO Les données du client à créer
     * @return Le client créé avec son ID
     * @throws EmailAlreadyExistsException si l'email existe déjà
     */
    public ClientResponseDTO createClient(ClientRequestDTO requestDTO) {
        log.info("Création d'un nouveau client avec l'email : {}", requestDTO.getEmail());

        // 1. Vérifier que l'email n'existe pas déjà
        if (clientRepository.existsByEmail(requestDTO.getEmail())) {
            log.error("Tentative de création avec un email existant : {}", requestDTO.getEmail());
            throw new EmailAlreadyExistsException(requestDTO.getEmail());
        }

        // 2. Convertir DTO → Entity
        ClientEntity clientEntity = clientMapper.toEntity(requestDTO);

        // 3. Sauvegarder en base de données
        ClientEntity savedClient = clientRepository.save(clientEntity);

        log.info("Client créé avec succès. ID : {}", savedClient.getId());

        // 4. Convertir Entity → DTO et retourner
        return clientMapper.toResponseDTO(savedClient);
    }

    /**
     * Récupérer tous les clients (version complète)
     *
     * @return Liste de tous les clients
     */
    @Transactional(readOnly = true)  // Optimisation pour les lectures seules
    public List<ClientResponseDTO> getAllClients() {
        log.info("Récupération de tous les clients");

        return clientRepository.findAll()
                .stream()
                .map(clientMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer tous les clients (version résumée pour les listes)
     *
     * @return Liste résumée de tous les clients
     */
    @Transactional(readOnly = true)
    public List<ClientSummaryDTO> getAllClientsSummary() {
        log.info("Récupération de tous les clients (résumé)");

        return clientRepository.findAll()
                .stream()
                .map(clientMapper::toSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer un client par son ID
     *
     * @param id L'identifiant du client
     * @return Le client trouvé
     * @throws ClientNotFoundException si le client n'existe pas
     */
    @Transactional(readOnly = true)
    public ClientResponseDTO getClientById(Long id) {
        log.info("Recherche du client avec l'ID : {}", id);

        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Client non trouvé avec l'ID : {}", id);
                    return new ClientNotFoundException(id);
                });

        return clientMapper.toResponseDTO(client);
    }

    /**
     * Récupérer un client par son email
     *
     * @param email L'email du client
     * @return Le client trouvé
     * @throws ClientNotFoundException si le client n'existe pas
     */
    @Transactional(readOnly = true)
    public ClientResponseDTO getClientByEmail(String email) {
        log.info("Recherche du client avec l'email : {}", email);

        ClientEntity client = clientRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Client non trouvé avec l'email : {}", email);
                    return new ClientNotFoundException("Client non trouvé avec l'email : " + email);
                });

        return clientMapper.toResponseDTO(client);
    }

    /**
     * Mettre à jour un client existant
     *
     * @param id L'identifiant du client à modifier
     * @param requestDTO Les nouvelles données
     * @return Le client mis à jour
     * @throws ClientNotFoundException si le client n'existe pas
     * @throws EmailAlreadyExistsException si le nouvel email existe déjà
     */
    public ClientResponseDTO updateClient(Long id, ClientRequestDTO requestDTO) {
        log.info("Mise à jour du client avec l'ID : {}", id);

        // 1. Vérifier que le client existe
        ClientEntity existingClient = clientRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Client non trouvé avec l'ID : {}", id);
                    return new ClientNotFoundException(id);
                });

        // 2. Vérifier que le nouvel email n'est pas déjà utilisé par un autre client
        if (!existingClient.getEmail().equals(requestDTO.getEmail()) &&
                clientRepository.existsByEmail(requestDTO.getEmail())) {
            log.error("Email déjà utilisé : {}", requestDTO.getEmail());
            throw new EmailAlreadyExistsException(requestDTO.getEmail());
        }

        // 3. Mettre à jour l'entité avec les nouvelles données
        clientMapper.updateEntity(existingClient, requestDTO);

        // 4. Sauvegarder les modifications
        ClientEntity updatedClient = clientRepository.save(existingClient);

        log.info("Client mis à jour avec succès. ID : {}", id);

        // 5. Retourner le client mis à jour
        return clientMapper.toResponseDTO(updatedClient);
    }

    /**
     * Supprimer un client
     *
     * @param id L'identifiant du client à supprimer
     * @throws ClientNotFoundException si le client n'existe pas
     */
    public void deleteClient(Long id) {
        log.info("Suppression du client avec l'ID : {}", id);

        // 1. Vérifier que le client existe
        if (!clientRepository.existsById(id)) {
            log.error("Impossible de supprimer : client non trouvé avec l'ID : {}", id);
            throw new ClientNotFoundException(id);
        }

        // 2. Supprimer le client
        clientRepository.deleteById(id);

        log.info("Client supprimé avec succès. ID : {}", id);
    }

    /**
     * Compter le nombre total de clients
     *
     * @return Le nombre de clients
     */
    @Transactional(readOnly = true)
    public long countClients() {
        long count = clientRepository.count();
        log.info("Nombre total de clients : {}", count);
        return count;
    }

    /**
     * Vérifier si un client existe par son ID
     *
     * @param id L'identifiant du client
     * @return true si le client existe, false sinon
     */
    @Transactional(readOnly = true)
    public boolean clientExists(Long id) {
        return clientRepository.existsById(id);
    }

    /**
     * Vérifier si un email est disponible
     *
     * @param email L'email à vérifier
     * @return true si l'email est disponible, false sinon
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !clientRepository.existsByEmail(email);
    }
}