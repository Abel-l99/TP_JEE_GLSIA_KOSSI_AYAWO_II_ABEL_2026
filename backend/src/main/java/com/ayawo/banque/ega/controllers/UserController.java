package com.ayawo.banque.ega.controllers;

import com.ayawo.banque.ega.dto.user.ChangePasswordDTO;
import com.ayawo.banque.ega.dto.user.UserRequestDTO;
import com.ayawo.banque.ega.dto.user.UserResponseDTO;
import com.ayawo.banque.ega.dto.user.UserUpdateDTO;
import com.ayawo.banque.ega.enums.Role;
import com.ayawo.banque.ega.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    /**
     * 1. CREATE - Créer un utilisateur
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody UserRequestDTO requestDTO) {

        UserResponseDTO createdUser = userService.createUser(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdUser);
    }

    /**
     * 2. READ ALL - Récupérer tous les utilisateurs
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {

        log.info("Requête GET /api/users - Récupération de tous les utilisateurs");

        List<UserResponseDTO> users = userService.getAllUsers();

        return ResponseEntity.ok(users);
    }

    /**
     * 3. READ BY ID - Récupérer un utilisateur par ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {

        UserResponseDTO user = userService.getUserById(id);

        return ResponseEntity.ok(user);
    }

    /**
     * 4. READ CURRENT USER
     * GET /users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        UserResponseDTO user = userService.getUserByUsername(username);

        return ResponseEntity.ok(user);
    }

    /**
     * 4. READ BY USERNAME
     * GET /api/users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDTO> getUserByUsername(@PathVariable String username) {

        log.info("Requête GET /api/users/username/{} - Récupération de l'utilisateur", username);

        UserResponseDTO user = userService.getUserByUsername(username);

        return ResponseEntity.ok(user);
    }

    /**
     * 5. READ BY ROLE - Récupérer les utilisateurs par rôle
     * GET /api/users/role/{role}
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@PathVariable Role role) {

        List<UserResponseDTO> users = userService.getUsersByRole(role);

        return ResponseEntity.ok(users);
    }

    /**
     * 6. UPDATE - Modifier un utilisateur
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO updateDTO) {

        UserResponseDTO updatedUser = userService.updateUser(id, updateDTO);

        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 7. CHANGE PASSWORD - Changer le mot de passe
     * PUT /api/users/{id}/change-password
     */
    @PutMapping("/{id}/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {

        log.info("Requête PUT /api/users/{}/change-password - Changement de mot de passe", id);

        userService.changePassword(id, changePasswordDTO);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Mot de passe changé avec succès");

        return ResponseEntity.ok(response);
    }

    /**
     * 8. DELETE - Supprimer un utilisateur
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {

        log.info("Requête DELETE /api/users/{} - Suppression de l'utilisateur", id);

        userService.deleteUser(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Utilisateur supprimé avec succès");
        response.put("id", id.toString());

        return ResponseEntity.ok(response);
    }

    /**
     * 9. TOGGLE STATUS - Activer/désactiver un utilisateur
     * PATCH /api/users/{id}/toggle-status
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<UserResponseDTO> toggleUserStatus(@PathVariable Long id) {

        log.info("Requête PATCH /api/users/{}/toggle-status - Changement de statut", id);

        UserResponseDTO user = userService.toggleUserStatus(id);

        return ResponseEntity.ok(user);
    }

    /**
     * 10. COUNT - Compter les utilisateurs
     * GET /api/users/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countUsers() {

        log.info("Requête GET /api/users/count - Comptage des utilisateurs");

        long count = userService.countUsers();

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * 11. COUNT BY ROLE - Compter les utilisateurs par rôle
     * GET /api/users/count/role/{role}
     */
    @GetMapping("/count/role/{role}")
    public ResponseEntity<Map<String, Long>> countUsersByRole(@PathVariable Role role) {

        log.info("Requête GET /api/users/count/role/{} - Comptage par rôle", role);

        long count = userService.countUsersByRole(role);

        Map<String, Long> response = new HashMap<>();
        response.put("role", (long) role.ordinal());
        response.put("count", count);

        return ResponseEntity.ok(response);
    }
}