package com.ayawo.banque.ega.exceptions.client;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(Long id) {
        super("Client non trouvé avec l'ID : " + id);
    }

    public ClientNotFoundException(String message) {
        super(message);
    }
}