import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { LoginService } from '../../services/login/login-service';
import { DashboardService } from '../../services/dashboard-service';

@Component({
  selector: 'app-dashboard-client',
  imports: [],
  templateUrl: './dashboard-client.html',
  styleUrl: './dashboard-client.scss',
})
export class DashboardClient implements OnInit {

  private loginService = inject(LoginService);
  private dashboardService = inject(DashboardService);
  private cdRef = inject(ChangeDetectorRef);

  user = this.loginService.user();

  comptes: any[] = [];
  statistiques = {
    totalComptes: 0,
    nombreCourant: 0,
    nombreEpargne: 0,
    sommeTotale: 0,
    sommeCourant: 0,
    sommeEpargne: 0
  };

  ngOnInit(): void {
    setTimeout(() => {
      const user = this.loginService.user();
      if (user && user.clientId) {
        console.log('Client ID trouvé:', user.clientId);
        
        this.dashboardService.getComptesClient(user.clientId).subscribe({
          next: (comptes) => {
            this.comptes = comptes;
            console.log('Comptes récupérés:', comptes);
            
            // CALCULE LES STATISTIQUES
            this.calculerStatistiques(comptes);

            this.cdRef.detectChanges();
          },
          error: (erreur) => {
            console.error('Erreur:', erreur);
          }
        });
      }
    }, 1500);
  }

  private calculerStatistiques(comptes: any[]): void {
    // 1. Nombre total
    this.statistiques.totalComptes = comptes.length;
    
    // 2. Comptes courant
    const comptesCourant = comptes.filter(c => c.typeCompte === 'COURANT');
    this.statistiques.nombreCourant = comptesCourant.length;
    
    // 3. Comptes épargne
    const comptesEpargne = comptes.filter(c => c.typeCompte === 'EPARGNE');
    this.statistiques.nombreEpargne = comptesEpargne.length;
    
    // 4. Sommes
    this.statistiques.sommeTotale = comptes.reduce((total, c) => total + (c.solde || 0), 0);
    this.statistiques.sommeCourant = comptesCourant.reduce((total, c) => total + (c.solde || 0), 0);
    this.statistiques.sommeEpargne = comptesEpargne.reduce((total, c) => total + (c.solde || 0), 0);
    
    // Affiche dans la console
    console.log('📊 STATISTIQUES :', this.statistiques);
  }
}