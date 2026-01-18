package com.ayawo.banque.ega.services;

import com.ayawo.banque.ega.entities.ClientEntity;
import com.ayawo.banque.ega.entities.CompteEntity;
import com.ayawo.banque.ega.entities.TransactionEntity;
import com.ayawo.banque.ega.enums.TypeTransaction;
import com.ayawo.banque.ega.exceptions.compte.CompteNotFoundException;
import com.ayawo.banque.ega.repositories.ClientRepository;
import com.ayawo.banque.ega.repositories.CompteRepository;
import com.ayawo.banque.ega.repositories.TransactionRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReleveService {

    private final ClientRepository clientRepository;
    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_SIMPLE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Générer un relevé bancaire pour UN compte spécifique
     */
    @Transactional(readOnly = true)
    public byte[] genererReleveCompte(String numeroCompte, LocalDateTime dateDebut, LocalDateTime dateFin) {
        log.info("Génération du relevé pour le compte {} du {} au {}", numeroCompte, dateDebut, dateFin);

        CompteEntity compte = compteRepository.findByNumeroCompte(numeroCompte)
                .orElseThrow(() -> new CompteNotFoundException(numeroCompte));

        List<TransactionEntity> transactions = transactionRepository
                .findByNumeroCompteAndDateBetween(numeroCompte, dateDebut, dateFin);

        BigDecimal soldeActuel = compte.getSolde();
        BigDecimal soldeInitial = calculerSoldeInitial(compte, dateDebut);

        return genererPDFCompte(compte, transactions, dateDebut, dateFin, soldeInitial, soldeActuel);
    }

    /**
     * Générer un relevé GLOBAL pour TOUS les comptes d'un client
     */
    @Transactional(readOnly = true)
    public byte[] genererReleveGlobalClient(Long clientId, LocalDateTime dateDebut, LocalDateTime dateFin) {
        log.info("Génération du relevé global pour le client {} du {} au {}", clientId, dateDebut, dateFin);

        // 1. Récupérer le client
        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'ID: " + clientId));

        // 2. Récupérer tous les comptes du client
        List<CompteEntity> comptes = compteRepository.findByProprietaireId(clientId);

        if (comptes.isEmpty()) {
            throw new RuntimeException("Aucun compte trouvé pour ce client");
        }

        // 3. Récupérer TOUTES les transactions du client en UNE SEULE requête
        List<TransactionEntity> toutesTransactions = transactionRepository
                .findByClientIdAndDateBetween(clientId, dateDebut, dateFin);

        log.info("✅ Total transactions trouvées pour le client: {}", toutesTransactions.size());

        // 4. Grouper les transactions par compte et calculer les totaux
        Map<CompteEntity, List<TransactionEntity>> transactionsParCompte = new LinkedHashMap<>();
        BigDecimal totalDepots = BigDecimal.ZERO;
        BigDecimal totalRetraits = BigDecimal.ZERO;
        BigDecimal soldeTotal = BigDecimal.ZERO;

        for (CompteEntity compte : comptes) {
            List<TransactionEntity> transactionsCompte = new ArrayList<>();

            for (TransactionEntity t : toutesTransactions) {
                boolean concerneCompte = false;

                // Vérifier si la transaction concerne ce compte (source)
                if (t.getCompteSource() != null &&
                        compte.getNumeroCompte().equals(t.getCompteSource().getNumeroCompte())) {
                    concerneCompte = true;
                    totalRetraits = totalRetraits.add(t.getMontant());
                }

                // Vérifier si la transaction concerne ce compte (destination)
                if (t.getCompteDestination() != null &&
                        compte.getNumeroCompte().equals(t.getCompteDestination().getNumeroCompte())) {
                    concerneCompte = true;
                    totalDepots = totalDepots.add(t.getMontant());
                }

                if (concerneCompte) {
                    transactionsCompte.add(t);
                }
            }

            log.info("📊 Compte {} : {} transactions", compte.getNumeroCompte(), transactionsCompte.size());

            transactionsParCompte.put(compte, transactionsCompte);
            soldeTotal = soldeTotal.add(compte.getSolde());
        }

        // 5. Générer le PDF
        return genererPDFGlobal(client, transactionsParCompte, dateDebut, dateFin,
                totalDepots, totalRetraits, soldeTotal);
    }

    /**
     * Calculer le solde initial à une date donnée
     */
    private BigDecimal calculerSoldeInitial(CompteEntity compte, LocalDateTime dateDebut) {
        BigDecimal soldeActuel = compte.getSolde();

        List<TransactionEntity> transactionsApres = transactionRepository
                .findByNumeroCompteAndDateBetween(compte.getNumeroCompte(), dateDebut, LocalDateTime.now());

        for (TransactionEntity transaction : transactionsApres) {
            if (transaction.getCompteSource() != null &&
                    transaction.getCompteSource().getNumeroCompte().equals(compte.getNumeroCompte())) {
                if (transaction.getType() == TypeTransaction.RETRAIT ||
                        transaction.getType() == TypeTransaction.VIREMENT) {
                    soldeActuel = soldeActuel.add(transaction.getMontant());
                } else if (transaction.getType() == TypeTransaction.DEPOT) {
                    soldeActuel = soldeActuel.subtract(transaction.getMontant());
                }
            }

            if (transaction.getCompteDestination() != null &&
                    transaction.getCompteDestination().getNumeroCompte().equals(compte.getNumeroCompte())) {
                soldeActuel = soldeActuel.subtract(transaction.getMontant());
            }
        }

        return soldeActuel;
    }

    /**
     * Générer le PDF pour UN compte
     */
    private byte[] genererPDFCompte(CompteEntity compte, List<TransactionEntity> transactions,
                                    LocalDateTime dateDebut, LocalDateTime dateFin,
                                    BigDecimal soldeInitial, BigDecimal soldeFinal) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            ajouterEnteteCompte(document, compte, dateDebut, dateFin);
            ajouterInfosCompte(document, compte, soldeInitial, soldeFinal);
            ajouterTableauTransactions(document, transactions, compte.getNumeroCompte());
            ajouterPiedDePage(document);

            document.close();
            log.info("Relevé généré avec succès pour le compte {}", compte.getNumeroCompte());

        } catch (Exception e) {
            log.error("Erreur lors de la génération du relevé PDF", e);
            throw new RuntimeException("Erreur lors de la génération du relevé", e);
        }

        return baos.toByteArray();
    }

    /**
     * Générer le PDF GLOBAL pour tous les comptes
     */
    private byte[] genererPDFGlobal(ClientEntity client,
                                    Map<CompteEntity, List<TransactionEntity>> transactionsParCompte,
                                    LocalDateTime dateDebut, LocalDateTime dateFin,
                                    BigDecimal totalDepots, BigDecimal totalRetraits,
                                    BigDecimal soldeTotal) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // En-tête
            ajouterEnteteGlobal(document, client, dateDebut, dateFin);

            // Résumé global
            ajouterResumeGlobal(document, transactionsParCompte.size(),
                    totalDepots, totalRetraits, soldeTotal);

            // Détail par compte
            for (Map.Entry<CompteEntity, List<TransactionEntity>> entry : transactionsParCompte.entrySet()) {
                ajouterDetailsCompte(document, entry.getKey(), entry.getValue());
            }

            // Pied de page
            ajouterPiedDePage(document);

            document.close();
            log.info("Relevé global généré avec succès pour le client {}", client.getNomComplet());

        } catch (Exception e) {
            log.error("Erreur lors de la génération du relevé global", e);
            throw new RuntimeException("Erreur lors de la génération du PDF global", e);
        }

        return baos.toByteArray();
    }

    // ========== EN-TÊTES ==========

    private void ajouterEnteteCompte(Document document, CompteEntity compte,
                                     LocalDateTime dateDebut, LocalDateTime dateFin) {
        document.add(new Paragraph("RELEVÉ BANCAIRE")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        document.add(new Paragraph("BANQUE EGA")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        document.add(new Paragraph(
                String.format("Période du %s au %s",
                        dateDebut.format(DATE_SIMPLE),
                        dateFin.format(DATE_SIMPLE)))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));
    }

    private void ajouterEnteteGlobal(Document document, ClientEntity client,
                                     LocalDateTime dateDebut, LocalDateTime dateFin) {
        document.add(new Paragraph("RELEVÉ BANCAIRE GLOBAL")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        document.add(new Paragraph("BANQUE EGA")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        document.add(new Paragraph("Client : " + client.getNomComplet())
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5));

        document.add(new Paragraph("Email : " + client.getEmail())
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        document.add(new Paragraph(
                String.format("Période du %s au %s",
                        dateDebut.format(DATE_SIMPLE),
                        dateFin.format(DATE_SIMPLE)))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));
    }

    // ========== INFOS COMPTE ==========

    private void ajouterInfosCompte(Document document, CompteEntity compte,
                                    BigDecimal soldeInitial, BigDecimal soldeFinal) {

        document.add(new Paragraph("INFORMATIONS DU COMPTE")
                .setFontSize(14)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(10));

        Table table = new Table(2);
        table.setWidth(UnitValue.createPercentValue(100));

        table.addCell(new Cell().add(new Paragraph("Titulaire :").setBold()));
        table.addCell(new Cell().add(new Paragraph(compte.getProprietaire().getNomComplet())));

        table.addCell(new Cell().add(new Paragraph("Numéro de compte :").setBold()));
        table.addCell(new Cell().add(new Paragraph(compte.getNumeroCompte())));

        table.addCell(new Cell().add(new Paragraph("Type de compte :").setBold()));
        table.addCell(new Cell().add(new Paragraph(compte.getTypeCompte().toString())));

        table.addCell(new Cell().add(new Paragraph("Solde initial :").setBold()));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f FCFA", soldeInitial))));

        table.addCell(new Cell().add(new Paragraph("Solde final :").setBold()));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f FCFA", soldeFinal)).setBold()));

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    // ========== RÉSUMÉ GLOBAL ==========

    private void ajouterResumeGlobal(Document document, int nombreComptes,
                                     BigDecimal totalDepots, BigDecimal totalRetraits,
                                     BigDecimal soldeTotal) {

        document.add(new Paragraph("RÉSUMÉ GLOBAL")
                .setFontSize(14)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(10));

        Table table = new Table(2);
        table.setWidth(UnitValue.createPercentValue(100));

        table.addCell(new Cell().add(new Paragraph("Nombre de comptes :").setBold()));
        table.addCell(new Cell().add(new Paragraph(String.valueOf(nombreComptes))));

        table.addCell(new Cell().add(new Paragraph("Total des dépôts :").setBold()));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f FCFA", totalDepots))
                .setFontColor(ColorConstants.GREEN)));

        table.addCell(new Cell().add(new Paragraph("Total des retraits :").setBold()));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f FCFA", totalRetraits))
                .setFontColor(ColorConstants.RED)));

        table.addCell(new Cell().add(new Paragraph("Solde total :").setBold()));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f FCFA", soldeTotal))
                        .setBold()
                        .setFontSize(12))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY));

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    // ========== DÉTAILS PAR COMPTE ==========

    private void ajouterDetailsCompte(Document document, CompteEntity compte,
                                      List<TransactionEntity> transactions) {

        document.add(new Paragraph("Compte " + compte.getTypeCompte() + " - N° " + compte.getNumeroCompte())
                .setFontSize(12)
                .setBold()
                .setMarginTop(15)
                .setMarginBottom(5));

        document.add(new Paragraph("Solde actuel : " + String.format("%.2f FCFA", compte.getSolde()))
                .setFontSize(10)
                .setBold()
                .setMarginBottom(10));

        if (transactions.isEmpty()) {
            document.add(new Paragraph("Aucune transaction sur cette période")
                    .setItalic()
                    .setFontSize(10)
                    .setMarginBottom(15));
            return;
        }

        ajouterTableauTransactions(document, transactions, compte.getNumeroCompte());
    }

    // ========== TABLEAU TRANSACTIONS ==========

    private void ajouterTableauTransactions(Document document, List<TransactionEntity> transactions,
                                            String numeroCompte) {

        if (transactions.isEmpty()) {
            document.add(new Paragraph("Aucune opération sur cette période.")
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER));
            return;
        }

        float[] columnWidths = {15f, 20f, 35f, 15f, 15f};
        Table table = new Table(columnWidths);
        table.setWidth(UnitValue.createPercentValue(100));

        // En-têtes
        table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Type").setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Description").setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Débit").setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        table.addHeaderCell(new Cell().add(new Paragraph("Crédit").setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));

        // Lignes
        for (TransactionEntity transaction : transactions) {
            table.addCell(new Cell().add(new Paragraph(transaction.getDate().format(DATE_FORMATTER)))
                    .setFontSize(10));

            table.addCell(new Cell().add(new Paragraph(transaction.getType().toString()))
                    .setFontSize(10));

            boolean estDebit = transaction.getCompteSource() != null &&
                    transaction.getCompteSource().getNumeroCompte().equals(numeroCompte);

            if (estDebit) {
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", transaction.getMontant())))
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontColor(ColorConstants.RED));
                table.addCell(new Cell().add(new Paragraph("-"))
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT));
            } else {
                table.addCell(new Cell().add(new Paragraph("-"))
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", transaction.getMontant())))
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontColor(ColorConstants.GREEN));
            }
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    // ========== PIED DE PAGE ==========

    private void ajouterPiedDePage(Document document) {
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Banque EGA - Tous droits réservés")
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));

        document.add(new Paragraph("Date d'édition : " + LocalDateTime.now().format(DATE_FORMATTER))
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER));
    }
}