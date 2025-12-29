package com.ayawo.banque.ega.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity,Long> {

    @Query("SELECT t FROM TransactionEntity t " +
            "WHERE t.compteSource.client.id = :clientId " +
            "   OR t.compteDestination.client.id = :clientId " +
            "ORDER BY t.dateTransaction DESC")
    List<TransactionEntity> findByClientId(@Param("clientId") Long clientId);

    // Dans TransactionRepository.java
    @Query("SELECT t FROM TransactionEntity t WHERE " +
            "(t.compteSource.numeroCompte = :numeroCompte OR " +
            "t.compteDestination.numeroCompte = :numeroCompte) AND " +
            "t.dateTransaction BETWEEN :debut AND :fin " +
            "ORDER BY t.dateTransaction ASC") // ASC pour remonter dans le temps
    List<TransactionEntity> findTransactionsByCompteAndPeriode(
            @Param("numeroCompte") String numeroCompte,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

}
