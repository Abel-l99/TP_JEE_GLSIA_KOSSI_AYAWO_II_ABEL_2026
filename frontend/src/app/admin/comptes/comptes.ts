import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { DashboardService } from '../../services/dashboard-service';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-comptes',
  imports: [DatePipe, FormsModule, CommonModule], 
  templateUrl: './comptes.html',
  styleUrl: './comptes.scss',
})
export class Comptes {

  dashboardService = inject(DashboardService);
  cdr = inject(ChangeDetectorRef);

  comptes: any[] = [];
  comptesFiltres: any[] = [];
  recherche: string = '';
  loading: boolean = false;

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
}