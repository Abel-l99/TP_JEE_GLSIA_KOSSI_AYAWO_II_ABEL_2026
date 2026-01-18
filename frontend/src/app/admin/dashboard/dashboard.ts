import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../services/dashboard-service';
import { Chart } from 'chart.js/auto';
import { LoginService } from '../../services/login/login-service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  
  private dashboardService = inject(DashboardService);
  private loginService = inject(LoginService);
  private router = inject(Router);
  
  private cdr = inject(ChangeDetectorRef);

  // ===== STATISTIQUES GLOBALES =====
  counts: number[] = [0, 0, 0]; // [clients, comptes, transactions]
  totalSolde: number = 0;
  
  // ===== TRANSACTIONS & GRAPHE =====
  transactions: any[] = [];
  
  moisLabels = [
    'Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Juin',
    'Juil', 'Août', 'Sep', 'Oct', 'Nov', 'Déc'
  ];
  
  // Pour le graphique ADMIN : toutes les transactions
  depotsParMois = new Array(12).fill(0);
  retraitsParMois = new Array(12).fill(0);
  virementsParMois = new Array(12).fill(0);
  
  chart: any;
  
  // ===== DÉTAILS SUPPLÉMENTAIRES =====
  statsDetails = {
    totalClients: 0,
    totalComptes: 0,
    totalTransactions: 0,
    soldeMoyenParCompte: 0,
    transactionsParMois: 0
  };

  constructor() {
    this.chargerDonneesGlobales();
  }

  ngOnInit() {
    const user = this.loginService.user();
    if (!user) {
      this.router.navigate(['/login']);
    }

    setTimeout(() => {
      this.chargerTransactionsGlobales();
    }, 1000);
  }
  
  // ===============================
  // CHARGER LES DONNÉES GLOBALES
  private chargerDonneesGlobales(): void {
    // 1. Compteurs globaux
    this.dashboardService.getAllCounts().subscribe(([clients, comptes, transactions]) => {
      this.counts = [
        (clients as any).count,
        (comptes as any).count,
        (transactions as any).count
      ];
      
      this.statsDetails.totalClients = (clients as any).count;
      this.statsDetails.totalComptes = (comptes as any).count;
      this.statsDetails.totalTransactions = (transactions as any).count;
      
      this.cdr.detectChanges();
    });

    // 2. Solde total
    this.dashboardService.getTotalSolde().subscribe(response => {
      this.totalSolde = (response as any).total;
      
      // Calcul du solde moyen par compte
      if (this.statsDetails.totalComptes > 0) {
        this.statsDetails.soldeMoyenParCompte = this.totalSolde / this.statsDetails.totalComptes;
      }
      
      this.cdr.detectChanges();
    });
  }
  
  // ===============================
  // CHARGER TOUTES LES TRANSACTIONS (pas juste d'un client)
  private chargerTransactionsGlobales(): void {
    // Tu dois créer cette méthode dans ton DashboardService
    this.dashboardService.getAllTransactions().subscribe({
      next: (transactions) => {
        this.transactions = transactions;
        console.log('📦 Toutes les transactions:', transactions.length);
        this.traiterTransactionsGlobales();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ Erreur chargement transactions globales:', err);
      }
    });
  }
  
  // ===============================
  // TRAITEMENT DES TRANSACTIONS GLOBALES
  private traiterTransactionsGlobales(): void {
    // Réinitialiser les tableaux
    this.depotsParMois.fill(0);
    this.retraitsParMois.fill(0);
    this.virementsParMois.fill(0);

    // Compter par mois pour TOUTES les transactions
    this.transactions.forEach(t => {
      const date = new Date(t.date);
      const mois = date.getMonth(); // 0 = Janvier
      const annee = date.getFullYear();
      
      // Vérifier que c'est l'année en cours (optionnel)
      const anneeCourante = new Date().getFullYear();
      if (annee === anneeCourante) {
        if (t.type === 'DEPOT') {
          this.depotsParMois[mois]++;
        } else if (t.type === 'RETRAIT') {
          this.retraitsParMois[mois]++;
        } else if (t.type === 'VIREMENT') {
          this.virementsParMois[mois]++;
        }
      }
    });

    // Calculer les transactions par mois (moyenne)
    const moisActuels = new Date().getMonth() + 1; // +1 car 0-indexé
    this.statsDetails.transactionsParMois = this.transactions.length / moisActuels;

    console.log('📊 ADMIN - Dépôts:', this.depotsParMois);
    console.log('📊 ADMIN - Retraits:', this.retraitsParMois);
    console.log('📊 ADMIN - Virements:', this.virementsParMois);

    this.afficherGraphiqueGlobal();
  }
  
  // ===============================
  // GRAPHE GLOBAL (admin)
  private afficherGraphiqueGlobal(): void {
    if (this.chart) {
      this.chart.destroy();
    }

    this.chart = new Chart('adminChart', {
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
      },
      options: {
        responsive: true,
        plugins: {
          title: {
            display: true,
            text: 'Transactions bancaires (tous clients)',
            font: {
              size: 16
            }
          },
          legend: {
            position: 'top',
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            title: {
              display: true,
              text: 'Nombre de transactions'
            }
          },
          x: {
            title: {
              display: true,
              text: 'Mois'
            }
          }
        }
      }
    });
  }
  
  // ===============================
  // MÉTHODES UTILITAIRES
  formaterMontant(montant: number): string {
    return new Intl.NumberFormat('fr-FR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(montant) + ' FCFA';
  }
  
  formaterNombre(nombre: number): string {
    return new Intl.NumberFormat('fr-FR').format(nombre);
  }

}