import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { LoginService } from '../../services/login/login-service';
import { DashboardService } from '../../services/dashboard-service';
import { Chart } from 'chart.js/auto';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard-client',
  templateUrl: './dashboard-client.html',
  styleUrl: './dashboard-client.scss',
})
export class DashboardClient implements OnInit {

  private loginService = inject(LoginService);
  private dashboardService = inject(DashboardService);
  private router = inject(Router);
  private cdRef = inject(ChangeDetectorRef);

  // ===== UTILISATEUR =====
  user = this.loginService.user();

  // ===== COMPTES & STATS =====
  comptes: any[] = [];

  statistiques = {
    totalComptes: 0,
    nombreCourant: 0,
    nombreEpargne: 0,
    sommeTotale: 0,
    sommeCourant: 0,
    sommeEpargne: 0
  };

  // ===== TRANSACTIONS & GRAPHE =====
  transactions: any[] = [];

  moisLabels = [
    'Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Juin',
    'Juil', 'Août', 'Sep', 'Oct', 'Nov', 'Déc'
  ];

  depotsParMois = new Array(12).fill(0);
  retraitsParMois = new Array(12).fill(0);

  chart: any;

  // ===============================
  ngOnInit(): void {

    // On attend que le user soit bien chargé
    setTimeout(() => {

      const user = this.loginService.user();

      if (user && user.clientId) {
        console.log('✅ Client ID:', user.clientId);

        // ===== COMPTES =====
        this.dashboardService.getComptesClient(user.clientId).subscribe({
          next: (comptes) => {
            this.comptes = comptes;
            this.calculerStatistiques(comptes);
            this.cdRef.detectChanges();
          },
          error: (err) => {
            console.error('❌ Erreur comptes:', err);
          }
        });

        // ===== TRANSACTIONS =====
        this.dashboardService.getTransactionsClient(user.clientId).subscribe({
          next: (transactions) => {
            this.transactions = transactions;
            console.log('📦 Transactions:', transactions);
            this.traiterTransactions();
          },
          error: (err) => {
            console.error('❌ Erreur transactions:', err);
          }
        });

      } else {
        console.error('❌ Utilisateur ou clientId introuvable');
        this.router.navigate(['/login']);
        return;
      }

    }, 1500);
  }

  // ===============================
  // TRAITEMENT TRANSACTIONS
  private traiterTransactions(): void {

    this.depotsParMois.fill(0);
    this.retraitsParMois.fill(0);

    this.transactions.forEach(t => {
      const mois = new Date(t.date).getMonth(); // 0 = Janvier

      if (t.type === 'DEPOT') {
        this.depotsParMois[mois]++;
      }

      if (t.type === 'RETRAIT') {
        this.retraitsParMois[mois]++;
      }
    });

    console.log('📊 Dépôts:', this.depotsParMois);
    console.log('📊 Retraits:', this.retraitsParMois);

    this.afficherGraphique();
  }

  // ===============================
  // GRAPHE
  private afficherGraphique(): void {

    if (this.chart) {
      this.chart.destroy();
    }

    this.chart = new Chart('transactionsChart', {
      type: 'bar',
      data: {
        labels: this.moisLabels,
        datasets: [
          {
            label: 'Dépôts',
            data: this.depotsParMois,
            backgroundColor: '#28a745',
            borderColor: '#218838',
            borderWidth: 1
          },
          {
            label: 'Retraits',
            data: this.retraitsParMois,
            backgroundColor: '#dc3545',
            borderColor: '#c82333',
            borderWidth: 1
          }
        ]
      }
    });
  }

  // ===============================
  // STATISTIQUES COMPTES
  private calculerStatistiques(comptes: any[]): void {

    this.statistiques.totalComptes = comptes.length;

    const comptesCourant = comptes.filter(c => c.typeCompte === 'COURANT');
    const comptesEpargne = comptes.filter(c => c.typeCompte === 'EPARGNE');

    this.statistiques.nombreCourant = comptesCourant.length;
    this.statistiques.nombreEpargne = comptesEpargne.length;

    this.statistiques.sommeTotale =
      comptes.reduce((total, c) => total + (c.solde || 0), 0);

    this.statistiques.sommeCourant =
      comptesCourant.reduce((total, c) => total + (c.solde || 0), 0);

    this.statistiques.sommeEpargne =
      comptesEpargne.reduce((total, c) => total + (c.solde || 0), 0);

    console.log('📊 STATISTIQUES:', this.statistiques);
  }
  
}
