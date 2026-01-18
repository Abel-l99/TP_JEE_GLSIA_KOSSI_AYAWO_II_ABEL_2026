package com.ayawo.banque.ega.exceptions.user;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("Utilisateur non trouvé avec l'ID");
    }

    public UserNotFoundException(String username) {
        super("Cette adresse e-mail n'est pas enregistré en tant que client");
    }
}