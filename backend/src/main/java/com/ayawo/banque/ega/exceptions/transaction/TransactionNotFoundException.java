package com.ayawo.banque.ega.exceptions.transaction;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(Long id) {
        super("Transaction non trouvée avec l'ID : " + id);
    }
}
