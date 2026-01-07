package com.ayawo.banque.ega.exceptions.compte;

public class CompteNotFoundException extends RuntimeException {

    public CompteNotFoundException(String numeroCompte) {
        super("Compte non trouvé avec le numéro : " + numeroCompte);
    }
}