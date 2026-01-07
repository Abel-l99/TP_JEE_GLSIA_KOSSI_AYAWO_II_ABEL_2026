package com.ayawo.banque.ega.repositories;

import com.ayawo.banque.ega.entities.CompteEntity;
import com.ayawo.banque.ega.enums.TypeCompte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompteRepository extends JpaRepository<CompteEntity, String> {

    List<CompteEntity> findByProprietaireId(Long clientId);

    Optional<CompteEntity> findByNumeroCompte(String numeroCompte);

    boolean existsByNumeroCompte(String numeroCompte);

    List<CompteEntity> findByTypeCompte(TypeCompte typeCompte);

    long countByProprietaireId(Long clientId);

    List<CompteEntity> findByProprietaireIdAndTypeCompte(Long clientId, TypeCompte typeCompte);

    @Query("SELECT SUM(c.solde) FROM CompteEntity c WHERE c.proprietaire.id = :clientId")
    BigDecimal sumSoldeByClientId(@Param("clientId") Long clientId);
}