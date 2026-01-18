import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { LoginService } from '../../services/login/login-service';
import { DashboardService } from '../../services/dashboard-service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../services/transaction-service';

@Component({
  selector: 'app-comptes-client',
  imports: [CommonModule, FormsModule],
  templateUrl: './comptes-client.html',
  styleUrl: './comptes-client.scss',
})
export class ComptesClient implements OnInit {
  
  private loginService = inject(LoginService);
  private dashboardService = inject(DashboardService);
  private transactionService = inject(TransactionService);
  cdr = inject(ChangeDetectorRef);
  
  // Données principales
  comptes: any[] = [];
  loading = true;
  error = '';
  recherche = '';
  
  // Dépôt
  compteSelectionne: any = null;
  montantDepot: number = 0;
  showSaisieDepot = false;
  showConfirmationDepot = false;
  
  // Retrait
  montantRetrait: number = 0;
  showSaisieRetrait = false;
  showConfirmationRetrait = false;
  
  // Virement
  montantVirement: number = 0;
  compteDestination: string = '';
  showSaisieVirement = false;
  showConfirmationVirement = false;

  ngOnInit(): void {
    this.chargerComptes();
  }

  chargerComptes(): void {
    this.loading = true;
    this.error = '';
    
    setTimeout(() => {
      const user = this.loginService.user();
      
      if (!user) {
        this.error = 'Utilisateur non connecté';
        this.loading = false;
        this.cdr.detectChanges();
        return;
      }
      
      if (!user.clientId) {
        this.error = 'ID client non trouvé';
        this.loading = false;
        this.cdr.detectChanges();
        return;
      }
      
      this.dashboardService.getComptesClient(user.clientId).subscribe({
        next: (comptes) => {
          this.comptes = comptes;
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.error = err.error?.message || 'Erreur lors du chargement des comptes';
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
    }, 1000);
  }

  // Filtre
  get comptesFiltres(): any[] {
    if (!this.recherche.trim()) return this.comptes;
    const rechercheLower = this.recherche.toLowerCase();
    return this.comptes.filter(compte =>
      (compte.numeroCompte?.toLowerCase().includes(rechercheLower)) ||
      (compte.typeCompte?.toLowerCase().includes(rechercheLower))
    );
  }

  getTypeBadgeClass(type: string): string {
    return type === 'COURANT' ? 'type-badge courant-badge' : 'type-badge epargne-badge';
  }

  getSoldeClass(solde: number): string {
    return solde >= 0 ? 'solde-positive' : 'solde-negative';
  }

  // === DÉPÔT ===
  preparerDepot(compte: any) {
    this.compteSelectionne = compte;
    this.montantDepot = 0;
    this.showSaisieDepot = true;
  }

  confirmerMontantDepot() {
    if (this.montantDepot <= 0) {
      alert("Montant invalide");
      return;
    }
    this.showSaisieDepot = false;
    this.showConfirmationDepot = true;
  }

  validerDepot() {
    const depotData = {
      type: "DEPOT",
      montant: this.montantDepot,
      numeroCompteSource: this.compteSelectionne.numeroCompte
    };
    
    this.transactionService.effectuerDepot(depotData).subscribe({
      next: () => {
        this.showConfirmationDepot = false;
        this.compteSelectionne = null;
        this.chargerComptes();
      },
      error: (error) => {
        console.error('Erreur dépôt', error);
        this.showConfirmationDepot = false;
      }
    });
  }

  annulerDepot() {
    this.showSaisieDepot = false;
    this.showConfirmationDepot = false;
    this.compteSelectionne = null;
  }

  // === RETRAIT ===
  preparerRetrait(compte: any) {
    this.compteSelectionne = compte;
    this.montantRetrait = 0;
    this.showSaisieRetrait = true;
  }

  confirmerMontantRetrait() {
    if (this.montantRetrait <= 0) {
      alert("Montant invalide");
      return;
    }
    this.showSaisieRetrait = false;
    this.showConfirmationRetrait = true;
  }

  validerRetrait() {
    const retraitData = {
      type: "RETRAIT",
      montant: this.montantRetrait,
      numeroCompteSource: this.compteSelectionne.numeroCompte
    };
    
    this.transactionService.effectuerRetrait(retraitData).subscribe({
      next: () => {
        this.showConfirmationRetrait = false;
        this.chargerComptes();
      },
      error: (error) => {
        console.error('Erreur retrait', error);
        this.showConfirmationRetrait = false;
      }
    });
  }

  annulerRetrait() {
    this.showSaisieRetrait = false;
    this.showConfirmationRetrait = false;
    this.compteSelectionne = null;
  }

  // === VIREMENT ===
  preparerVirement(compte: any) {
    this.compteSelectionne = compte;
    this.montantVirement = 0;
    this.compteDestination = '';
    this.showSaisieVirement = true;
  }

  confirmerVirement() {
    if (this.montantVirement <= 0) {
      alert("Montant invalide");
      return;
    }
    if (!this.compteDestination) {
      alert("Compte destinataire requis");
      return;
    }
    this.showSaisieVirement = false;
    this.showConfirmationVirement = true;
  }

  validerVirement() {
    const virementData = {
      type: "VIREMENT",
      montant: this.montantVirement,
      numeroCompteSource: this.compteSelectionne.numeroCompte,
      numeroCompteDestination: this.compteDestination
    };
    
    this.transactionService.effectuerVirement(virementData).subscribe({
      next: () => {
        this.showConfirmationVirement = false;
        this.chargerComptes();
      },
      error: (error) => {
        console.error('Erreur virement', error);
        this.showConfirmationVirement = false;
      }
    });
  }

  annulerVirement() {
    this.showSaisieVirement = false;
    this.showConfirmationVirement = false;
    this.compteSelectionne = null;
  }

  // === UTILITAIRES ===
  rafraichir(): void {
    this.chargerComptes();
  }

}