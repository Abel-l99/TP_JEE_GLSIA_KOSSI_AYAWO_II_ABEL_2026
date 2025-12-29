package com.ayawo.banque.ega.Releve;

import com.ayawo.banque.ega.Compte.CompteEntity;
import com.ayawo.banque.ega.Transaction.TransactionEntity;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ReleveService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] genererRelevePDF(
            CompteEntity compte,
            List<TransactionEntity> transactions,
            LocalDate dateDebut,
            LocalDate dateFin) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            // 1. Créer le document PDF
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 2. Ajouter l'en-tête
            ajouterEnTete(document, compte, dateDebut, dateFin);

            // 3. Ajouter les informations du compte
            ajouterInfosCompte(document, compte, dateDebut);

            // 4. Ajouter le tableau des transactions
            if (transactions != null && !transactions.isEmpty()) {
                ajouterTableauTransactions(document, transactions, compte);
            } else {
                document.add(new Paragraph("Aucune transaction sur cette période.")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(12));
            }

            // 5. Ajouter le récapitulatif
            if (transactions != null && !transactions.isEmpty()) {
                ajouterRecapitulatif(document, transactions, compte);
            }

            // 6. Ajouter le pied de page
            ajouterPiedPage(document);

            document.close();

        } catch (Exception e) {
            throw new IOException("Erreur lors de la génération du PDF", e);
        }

        return baos.toByteArray();
    }

    private void ajouterEnTete(Document document, CompteEntity compte,
                               LocalDate dateDebut, LocalDate dateFin) throws IOException {

        // Police pour le titre
        PdfFont fontTitre = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Titre principal
        Paragraph titre = new Paragraph("RELEVÉ BANCAIRE")
                .setFont(fontTitre)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);

        // Nom de la banque
        Paragraph banque = new Paragraph("EGA BANK - Système de Gestion Bancaire")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);

        // Période
        Paragraph periode = new Paragraph(
                String.format("Période : %s au %s",
                        dateDebut.format(DATE_FORMATTER),
                        dateFin.format(DATE_FORMATTER)))
                .setFontSize(11)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);

        // Ligne de séparation
        LineSeparator ligne = new LineSeparator(new SolidLine());
        ligne.setMarginBottom(20);

        document.add(titre);
        document.add(banque);
        document.add(periode);
        document.add(ligne);
    }

    private void ajouterInfosCompte(Document document, CompteEntity compte,
                                    LocalDate dateDebut) {

        // Section Informations Client
        Paragraph sectionInfo = new Paragraph("INFORMATIONS CLIENT")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(10);

        // Tableau d'informations
        float[] largeurColonnes = {200, 300};
        Table tableInfos = new Table(UnitValue.createPercentArray(largeurColonnes));
        tableInfos.setWidth(UnitValue.createPercentValue(100));

        ajouterLigneTableau(tableInfos, "Nom du client :",
                compte.getClient().getNom() + " " + compte.getClient().getPrenom());
        ajouterLigneTableau(tableInfos, "Numéro de compte :", compte.getNumeroCompte());
        ajouterLigneTableau(tableInfos, "Type de compte :", compte.getTypeCompte());
        ajouterLigneTableau(tableInfos, "Date d'ouverture :",
                compte.getDateCreation().format(DATE_FORMATTER));
        ajouterLigneTableau(tableInfos, "Date d'édition :",
                LocalDate.now().format(DATE_FORMATTER));

        document.add(sectionInfo);
        document.add(tableInfos);

        // Espacement
        document.add(new Paragraph(" ").setMarginBottom(20));
    }

    private void ajouterTableauTransactions(Document document,
                                            List<TransactionEntity> transactions,
                                            CompteEntity compte) {

        // Titre section
        Paragraph titreSection = new Paragraph("DÉTAIL DES OPÉRATIONS")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(10);

        document.add(titreSection);

        // Tableau des transactions
        float[] largeurColonnes = {80, 100, 150, 100, 100};
        Table table = new Table(UnitValue.createPercentArray(largeurColonnes));
        table.setWidth(UnitValue.createPercentValue(100));

        // En-tête du tableau
        table.addHeaderCell(creerCellule("Date", true));
        table.addHeaderCell(creerCellule("Heure", true));
        table.addHeaderCell(creerCellule("Type / Référence", true));
        table.addHeaderCell(creerCellule("Débit", true));
        table.addHeaderCell(creerCellule("Crédit", true));

        // Calculer le solde au fil des transactions
        BigDecimal soldeCourant = compte.getSolde(); // On part du solde final et on remonte

        // Parcourir les transactions (triées par date décroissante)
        for (TransactionEntity transaction : transactions) {
            String date = transaction.getDateTransaction().format(DATE_FORMATTER);
            String heure = transaction.getDateTransaction().format(
                    DateTimeFormatter.ofPattern("HH:mm"));

            String type = transaction.getTypeTransaction().toUpperCase();
            String reference = "Ref: " + transaction.getId();

            BigDecimal montant = transaction.getMontant();
            String debit = "";
            String credit = "";

            // Déterminer si c'est un débit ou un crédit
            if ("retrait".equalsIgnoreCase(type) ||
                    ("virement".equalsIgnoreCase(type) &&
                            transaction.getCompteSource().getNumeroCompte().equals(compte.getNumeroCompte()))) {
                // Débit
                debit = formatMontant(montant);
                soldeCourant = soldeCourant.add(montant); // On ajoute car on remonte dans le temps
            } else {
                // Crédit (dépôt ou virement reçu)
                credit = formatMontant(montant);
                soldeCourant = soldeCourant.subtract(montant);
            }

            // Ajouter la ligne
            table.addCell(creerCellule(date, false));
            table.addCell(creerCellule(heure, false));
            table.addCell(creerCellule(type + "\n" + reference, false));
            table.addCell(creerCellule(debit, false));
            table.addCell(creerCellule(credit, false));
        }

        document.add(table);
        document.add(new Paragraph(" ").setMarginBottom(20));
    }

    private void ajouterRecapitulatif(Document document,
                                      List<TransactionEntity> transactions,
                                      CompteEntity compte) {

        // Calculer les totaux
        BigDecimal totalDepots = BigDecimal.ZERO;
        BigDecimal totalRetraits = BigDecimal.ZERO;
        BigDecimal totalVirementsEnvoyes = BigDecimal.ZERO;
        BigDecimal totalVirementsRecus = BigDecimal.ZERO;

        for (TransactionEntity transaction : transactions) {
            String type = transaction.getTypeTransaction();
            BigDecimal montant = transaction.getMontant();

            if ("depot".equalsIgnoreCase(type)) {
                totalDepots = totalDepots.add(montant);
            } else if ("retrait".equalsIgnoreCase(type)) {
                totalRetraits = totalRetraits.add(montant);
            } else if ("virement".equalsIgnoreCase(type)) {
                if (transaction.getCompteSource().getNumeroCompte()
                        .equals(compte.getNumeroCompte())) {
                    totalVirementsEnvoyes = totalVirementsEnvoyes.add(montant);
                } else {
                    totalVirementsRecus = totalVirementsRecus.add(montant);
                }
            }
        }

        // Titre section
        Paragraph titreSection = new Paragraph("RÉCAPITULATIF")
                .setFontSize(12)
                .setBold()
                .setMarginBottom(10);

        // Tableau récapitulatif
        float[] largeurColonnes = {300, 200};
        Table tableRecap = new Table(UnitValue.createPercentArray(largeurColonnes));
        tableRecap.setWidth(UnitValue.createPercentValue(100));

        ajouterLigneTableau(tableRecap, "Nombre total d'opérations :",
                String.valueOf(transactions.size()));
        ajouterLigneTableau(tableRecap, "Total des dépôts :",
                formatMontant(totalDepots));
        ajouterLigneTableau(tableRecap, "Total des retraits :",
                formatMontant(totalRetraits));
        ajouterLigneTableau(tableRecap, "Total des virements envoyés :",
                formatMontant(totalVirementsEnvoyes));
        ajouterLigneTableau(tableRecap, "Total des virements reçus :",
                formatMontant(totalVirementsRecus));

        BigDecimal totalCredits = totalDepots.add(totalVirementsRecus);
        BigDecimal totalDebits = totalRetraits.add(totalVirementsEnvoyes);
        BigDecimal soldeFinal = totalCredits.subtract(totalDebits);

        ajouterLigneTableau(tableRecap, "Total des mouvements créditeurs :",
                formatMontant(totalCredits));
        ajouterLigneTableau(tableRecap, "Total des mouvements débiteurs :",
                formatMontant(totalDebits));

        // Ligne de séparation avant le solde final
        tableRecap.addCell(creerCellule(" ", false).setBorderTop(null));
        tableRecap.addCell(creerCellule(" ", false).setBorderTop(null));

        // Solde final en gras
        tableRecap.addCell(creerCellule("SOLDE FINAL :", true)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY));
        tableRecap.addCell(creerCellule(formatMontant(compte.getSolde()), true)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setFontColor(ColorConstants.BLUE));

        document.add(titreSection);
        document.add(tableRecap);
        document.add(new Paragraph(" ").setMarginBottom(20));
    }

    private void ajouterPiedPage(Document document) {

        // Ligne de séparation
        LineSeparator ligne = new LineSeparator(new SolidLine());
        ligne.setMarginTop(20).setMarginBottom(10);

        // Texte du pied de page
        Paragraph piedPage = new Paragraph()
                .setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .add("Document généré électroniquement le " +
                        LocalDate.now().format(DATE_FORMATTER) + "\n")
                .add("EGA Bank - Siège social: Lomé, Togo\n")
                .add("Tél: +228 22 22 22 22 - Email: contact@egabank.tg\n")
                .add("Ce document a valeur de justificatif bancaire.");

        document.add(ligne);
        document.add(piedPage);
    }

    // Méthodes utilitaires
    private Cell creerCellule(String texte, boolean estEnTete) {
        Cell cellule = new Cell().add(new Paragraph(texte));
        cellule.setPadding(5);

        if (estEnTete) {
            cellule.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cellule.setBold();
        }

        return cellule;
    }

    private void ajouterLigneTableau(Table table, String label, String valeur) {
        table.addCell(creerCellule(label, false));
        table.addCell(creerCellule(valeur, false));
    }

    private String formatMontant(BigDecimal montant) {
        if (montant == null) return "0,00";
        NumberFormat format = NumberFormat.getNumberInstance(Locale.FRANCE);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        return format.format(montant) + " FCFA";
    }
}