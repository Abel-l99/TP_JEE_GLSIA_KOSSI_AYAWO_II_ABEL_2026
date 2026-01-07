package com.ayawo.banque.ega.repositories;

import com.ayawo.banque.ega.entities.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<ClientEntity> findByNomAndPrenom(String nom, String prenom);

}
