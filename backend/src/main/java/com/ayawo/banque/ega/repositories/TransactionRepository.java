package com.ayawo.banque.ega.repositories;

import com.ayawo.banque.ega.entities.TransactionEntity;
import com.ayawo.banque.ega.enums.TypeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    // Trouver toutes les transactions d'un compte (source ou destination)
    @Query("SELECT t FROM TransactionEntity t WHERE t.compteSource.numeroCompte = :numeroCompte OR t.compteDestination.numeroCompte = :numeroCompte ORDER BY t.date DESC")
    List<TransactionEntity> findByNumeroCompte(@Param("numeroCompte") String numeroCompte);

    // Trouver les transactions d'un compte par type
    List<TransactionEntity> findByCompteSourceNumeroCompteAndType(String numeroCompte, TypeTransaction type);

    // Trouver les transactions entre deux dates pour un compte
    @Query("SELECT t FROM TransactionEntity t WHERE (t.compteSource.numeroCompte = :numeroCompte OR t.compteDestination.numeroCompte = :numeroCompte) AND t.date BETWEEN :dateDebut AND :dateFin ORDER BY t.date DESC")
    List<TransactionEntity> findByNumeroCompteAndDateBetween(
            @Param("numeroCompte") String numeroCompte,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );

    @Query("SELECT t FROM TransactionEntity t WHERE t.compteSource.proprietaire.id = :clientId OR t.compteDestination.proprietaire.id = :clientId ORDER BY t.date DESC")
    List<TransactionEntity> findByClientId(@Param("clientId") Long clientId);

    // ✅ NOUVELLE MÉTHODE : Trouver les transactions d'un client sur une période
    @Query("SELECT t FROM TransactionEntity t WHERE (t.compteSource.proprietaire.id = :clientId OR t.compteDestination.proprietaire.id = :clientId) AND t.date BETWEEN :dateDebut AND :dateFin ORDER BY t.date DESC")
    List<TransactionEntity> findByClientIdAndDateBetween(
            @Param("clientId") Long clientId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );

}