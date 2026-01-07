package com.ayawo.banque.ega.entities;

import com.ayawo.banque.ega.enums.TypeCompte;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "comptes")
public class CompteEntity {

    @Id
    @Column(name = "numero_compte", unique = true, nullable = false, length = 34)
    private String numeroCompte;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_compte", nullable = false, length = 10, updatable = false)
    private TypeCompte typeCompte;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal solde = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore
    private ClientEntity proprietaire;

    @OneToMany(mappedBy = "compteSource", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TransactionEntity> transactions = new ArrayList<>();

    public void crediter(BigDecimal montant) {
        this.solde = this.solde.add(montant);
    }

    public void debiter(BigDecimal montant) {
        this.solde = this.solde.subtract(montant);
    }

    public boolean soldeEstSuffisant(BigDecimal montant) {
        return this.solde.compareTo(montant) >= 0;
    }

}
