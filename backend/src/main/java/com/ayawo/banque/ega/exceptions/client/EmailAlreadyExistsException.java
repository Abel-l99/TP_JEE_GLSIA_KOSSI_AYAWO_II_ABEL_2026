package com.ayawo.banque.ega.exceptions.client;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Un client existe déjà avec l'email : " + email);
    }
}