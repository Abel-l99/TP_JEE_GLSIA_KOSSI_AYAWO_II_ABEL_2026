package com.ayawo.banque.ega.services;

import com.ayawo.banque.ega.dto.user.ChangePasswordDTO;
import com.ayawo.banque.ega.dto.user.UserRequestDTO;
import com.ayawo.banque.ega.dto.user.UserResponseDTO;
import com.ayawo.banque.ega.dto.user.UserUpdateDTO;
import com.ayawo.banque.ega.entities.ClientEntity;
import com.ayawo.banque.ega.entities.UserEntity;
import com.ayawo.banque.ega.enums.Role;
import com.ayawo.banque.ega.exceptions.client.ClientNotFoundException;
import com.ayawo.banque.ega.exceptions.user.InvalidPasswordException;
import com.ayawo.banque.ega.exceptions.user.UserNotFoundException;
import com.ayawo.banque.ega.exceptions.user.UsernameAlreadyExistsException;
import com.ayawo.banque.ega.mappers.UserMapper;
import com.ayawo.banque.ega.repositories.ClientRepository;
import com.ayawo.banque.ega.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 1. CREATE - Créer un utilisateur
     */
    public UserResponseDTO createUser(UserRequestDTO requestDTO) {

        // 1. Vérifier que le username (email) n'existe pas déjà
        if (userRepository.existsByUsername(requestDTO.getUsername())) {
            throw new UsernameAlreadyExistsException(requestDTO.getUsername());
        }

        // 2. Trouver le client par son email (username = email)
        ClientEntity client = clientRepository.findByEmail(requestDTO.getUsername())
                .orElseThrow(() -> new ClientNotFoundException(
                        "Aucun client trouvé avec cette adresse"
                ));

        // 3. Vérifier que le client n'a pas déjà un compte utilisateur
        if (client.getUser() != null) {
            throw new IllegalArgumentException(
                    "Vous avez déjà un compte utilisateur"
            );
        }

        // 4. Créer l'utilisateur
        UserEntity user = UserEntity.builder()
                .username(requestDTO.getUsername())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .role(requestDTO.getRole() != null ? requestDTO.getRole() : Role.CLIENT) // Par défaut CLIENT
                .active(true)
                .build();

        UserEntity savedUser = userRepository.save(user);

        // 5. Lier le client à l'utilisateur
        client.setUser(savedUser);
        clientRepository.save(client);

        return userMapper.toResponseDTO(savedUser);
    }

    /**
     * 2. READ ALL - Récupérer tous les utilisateurs
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.info("Récupération de tous les utilisateurs");

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 3. READ BY ID - Récupérer un utilisateur par ID
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        log.info("Recherche de l'utilisateur ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userMapper.toResponseDTO(user);
    }

    /**
     * 4. READ BY USERNAME
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        log.info("Recherche de l'utilisateur : {}", username);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        return userMapper.toResponseDTO(user);
    }

    /**
     * 5. READ BY ROLE - Récupérer les utilisateurs par rôle
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getUsersByRole(Role role) {
        log.info("Récupération des utilisateurs avec le rôle : {}", role);

        return userRepository.findByRole(role)
                .stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 6. UPDATE - Modifier un utilisateur
     */
    public UserResponseDTO updateUser(Long id, UserUpdateDTO updateDTO) {
        log.info("Mise à jour de l'utilisateur ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Modifier le mot de passe si fourni
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isBlank()) {
            user.setPassword(updateDTO.getPassword());  // TODO: Hasher avec BCrypt
        }

        // Activer/désactiver le compte
        if (updateDTO.getActive() != null) {
            user.setActive(updateDTO.getActive());
        }

        UserEntity updatedUser = userRepository.save(user);

        log.info("Utilisateur mis à jour avec succès");

        return userMapper.toResponseDTO(updatedUser);
    }

    /**
     * 7. CHANGE PASSWORD - Changer le mot de passe
     */
    public void changePassword(Long id, ChangePasswordDTO changePasswordDTO) {
        log.info("Changement de mot de passe pour l'utilisateur ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Vérifier l'ancien mot de passe
        if (!user.getPassword().equals(changePasswordDTO.getOldPassword())) {
            log.error("Ancien mot de passe incorrect pour l'utilisateur ID: {}", id);
            throw new InvalidPasswordException();
        }

        // Mettre à jour avec le nouveau mot de passe
        user.setPassword(changePasswordDTO.getNewPassword());  // TODO: Hasher avec BCrypt
        userRepository.save(user);

        log.info("Mot de passe changé avec succès pour l'utilisateur ID: {}", id);
    }

    /**
     * 8. DELETE - Supprimer un utilisateur
     */
    public void deleteUser(Long id) {
        log.info("Suppression de l'utilisateur ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Si c'est un CLIENT, déconnecter le client
        if (user.getClient() != null) {
            ClientEntity client = user.getClient();
            client.setUser(null);
            clientRepository.save(client);
        }

        userRepository.delete(user);

        log.info("Utilisateur supprimé avec succès");
    }

    /**
     * 9. ACTIVATE/DEACTIVATE - Activer/désactiver un utilisateur
     */
    public UserResponseDTO toggleUserStatus(Long id) {
        log.info("Changement du statut de l'utilisateur ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setActive(!user.isActive());
        UserEntity updatedUser = userRepository.save(user);

        log.info("Statut de l'utilisateur modifié : {}", updatedUser.isActive() ? "Actif" : "Inactif");

        return userMapper.toResponseDTO(updatedUser);
    }

    /**
     * 10. UPDATE LAST LOGIN - Mettre à jour la dernière connexion
     */
    public void updateLastLogin(Long id) {
        log.info("Mise à jour de la dernière connexion pour l'utilisateur ID: {}", id);

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 11. COUNT - Compter les utilisateurs
     */
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * 12. COUNT BY ROLE - Compter les utilisateurs par rôle
     */
    @Transactional(readOnly = true)
    public long countUsersByRole(Role role) {
        return userRepository.findByRole(role).size();
    }
}