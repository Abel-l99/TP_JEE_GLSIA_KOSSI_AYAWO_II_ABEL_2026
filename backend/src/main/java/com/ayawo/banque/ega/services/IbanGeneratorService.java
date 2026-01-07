package com.ayawo.banque.ega.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class IbanGeneratorService {

    private static final String COUNTRY_CODE = "TG";
    private static final String CHECK_DIGITS = "76";
    private static final String BANK_CODE = "10278";

    /**
     * Génère un numéro IBAN simplifié (sans validation stricte)
     * Format : FR76 1027 8XXX XXXX XXXX XXXX XXX
     *
     * @return Un numéro IBAN
     */
    public String generateIban() {
        try {
            // Générer un code guichet (5 chiffres)
            String branchCode = generateRandomDigits(5);

            // Générer un numéro de compte (11 chiffres)
            String accountNumber = generateRandomDigits(11);

            // Générer la clé RIB (2 chiffres)
            String ribKey = generateRandomDigits(2);

            // Construire l'IBAN : FR76 + BankCode(5) + Branch(5) + Account(11) + Key(2)
            String iban = COUNTRY_CODE + CHECK_DIGITS + BANK_CODE + branchCode + accountNumber + ribKey;

            log.info("IBAN généré : {}", iban);

            return iban;

        } catch (Exception e) {
            log.error("Erreur lors de la génération de l'IBAN : {}", e.getMessage(), e);
            throw new RuntimeException("Impossible de générer un IBAN", e);
        }
    }

    /**
     * Génère une chaîne de chiffres aléatoires
     */
    private String generateRandomDigits(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }

    /**
     * Valide basiquement un IBAN (format)
     */
    public boolean isValidIban(String iban) {
        if (iban == null || iban.isBlank()) {
            return false;
        }

        // Vérification simple : commence par FR et a 27 caractères
        return iban.startsWith("TG") && iban.length() == 27;
    }
}