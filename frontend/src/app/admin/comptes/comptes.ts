import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { DashboardService } from '../../services/dashboard-service';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../services/transaction-service';

@Component({
  selector: 'app-comptes',
  imports: [DatePipe, FormsModule, CommonModule], 
  templateUrl: './comptes.html',
  styleUrl: './comptes.scss',
})
export class Comptes {

  dashboardService = inject(DashboardService);
  transactionService = inject(TransactionService);
  cdr = inject(ChangeDetectorRef);

  comptes: any[] = [];
  comptesFiltres: any[] = [];
  recherche: string = '';
  loading: boolean = false;

  // Dépôt
  compteSelectionne: any = null;
  montantDepot: number = 0;
  showSaisieDepot = false;
  showConfirmationDepot = false;
  
  // Retrait
  montantRetrait: number = 0;
  showSaisieRetrait = false;
  showConfirmationRetrait = false;

  ngOnInit(): void {
    this.loadComptes();
  }

  loadComptes() {
    this.loading = true;
    this.dashboardService.getComptesSummary().subscribe({
      next: (comptes) => {
        this.comptes = comptes;
        this.comptesFiltres = [...comptes];
        console.log('Comptes chargés:', this.comptes);
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur chargement comptes:', error);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  // Méthode pour filtrer les comptes
  filtrerComptes(): void {
    if (!this.recherche.trim()) {
      this.comptesFiltres = [...this.comptes];
      return;
    }

    const terme = this.recherche.toLowerCase().trim();
    
    this.comptesFiltres = this.comptes.filter(compte => 
      (compte.numeroCompte && compte.numeroCompte.toLowerCase().includes(terme)) ||
      (compte.proprietaireNom && compte.proprietaireNom.toLowerCase().includes(terme)) ||
      (compte.typeCompte && compte.typeCompte.toLowerCase().includes(terme))
    );
    
    this.cdr.detectChanges();
  }

  // Appelée quand l'input change
  onRechercheChange(): void {
    this.filtrerComptes();
  }

  // Réinitialiser la recherche
  reinitialiserRecherche(): void {
    this.recherche = '';
    this.filtrerComptes();
  }

  // Formater le type de compte
  formaterTypeCompte(type: string): string {
    return type === 'COURANT' ? 'Compte Courant' : 
           type === 'EPARGNE' ? 'Compte Épargne' : type;
  }

  // Récupérer le badge CSS pour le type
  getTypeBadgeClass(type: string): string {
    return type === 'COURANT' ? 'badge-courant' : 'badge-epargne';
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
        this.loadComptes();
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
        this.loadComptes();
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

}