package com.ayawo.banque.ega.exceptions.user;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Vous avez déjà un compte utilisateur");
    }
}