package com.ayawo.banque.ega.Releve;

import com.ayawo.banque.ega.Compte.CompteEntity;
import com.ayawo.banque.ega.Compte.CompteRepository;
import com.ayawo.banque.ega.Releve.ReleveService;
import com.ayawo.banque.ega.Transaction.TransactionEntity;
import com.ayawo.banque.ega.Transaction.TransactionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/releve")
public class ReleveController {

    private final ReleveService releveService;
    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;

    public ReleveController(ReleveService releveService,
                            CompteRepository compteRepository,
                            TransactionRepository transactionRepository) {
        this.releveService = releveService;
        this.compteRepository = compteRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * GET /releve/pdf/{numeroCompte}
     * Génère et télécharge un relevé PDF pour un compte
     *
     * Paramètres :
     * - dateDebut : Date de début (optionnel, défaut = début du mois)
     * - dateFin : Date de fin (optionnel, défaut = aujourd'hui)
     */
    @GetMapping("/pdf/{numeroCompte}")
    public ResponseEntity<byte[]> genererRelevePDF(
            @PathVariable String numeroCompte,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        try {
            // 1. Vérifier que le compte existe
            Optional<CompteEntity> compteOpt = compteRepository.findById(numeroCompte);
            if (!compteOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            CompteEntity compte = compteOpt.get();

            // 2. Définir les dates par défaut si non fournies
            if (dateDebut == null) {
                dateDebut = LocalDate.now().withDayOfMonth(1); // Premier du mois
            }

            if (dateFin == null) {
                dateFin = LocalDate.now(); // Aujourd'hui
            }

            // 3. Récupérer les transactions sur la période
            List<TransactionEntity> transactions = transactionRepository
                    .findTransactionsByCompteAndPeriode(
                            numeroCompte,
                            dateDebut.atStartOfDay(),
                            dateFin.atTime(23, 59, 59));

            // 4. Générer le PDF
            byte[] pdfBytes = releveService.genererRelevePDF(
                    compte, transactions, dateDebut, dateFin);

            // 5. Préparer la réponse pour téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "releve_" + numeroCompte + "_" + LocalDate.now() + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /releve/html/{numeroCompte}
     * Version HTML simple pour prévisualisation
     */
    @GetMapping("/html/{numeroCompte}")
    public ResponseEntity<String> genererReleveHTML(
            @PathVariable String numeroCompte,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {

        // Implémentation similaire mais retourne du HTML
        // ...

        return ResponseEntity.ok("<html>...relevé HTML...</html>");
    }
}