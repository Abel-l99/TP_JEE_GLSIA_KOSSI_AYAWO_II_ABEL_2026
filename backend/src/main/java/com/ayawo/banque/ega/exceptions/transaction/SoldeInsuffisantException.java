package com.ayawo.banque.ega.exceptions.transaction;

import java.math.BigDecimal;

public class SoldeInsuffisantException extends RuntimeException {
    public SoldeInsuffisantException(BigDecimal solde, BigDecimal montant) {
        super(String.format("Solde insuffisant. Solde actuel: %.2f, Montant demandé: %.2f", solde, montant));
    }
}