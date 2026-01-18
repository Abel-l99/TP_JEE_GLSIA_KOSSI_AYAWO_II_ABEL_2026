import { ChangeDetectorRef, Component, inject, NgModule, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoginService } from '../../services/login/login-service';
import { DashboardService } from '../../services/dashboard-service';
import { TransactionService } from '../../services/transaction-service';

import { MatIconModule } from '@angular/material/icon';

NgModule({
  imports: [MatIconModule]
})

@Component({
  selector: 'app-transactions-client',
  imports: [CommonModule, FormsModule],
  templateUrl: './transactions-client.html',
  styleUrl: './transactions-client.scss',
})
export class TransactionsClient implements OnInit {
  // Services
  private loginService = inject(LoginService);
  private dashboardService = inject(DashboardService);
  private transactionService = inject(TransactionService);
  private cdr = inject(ChangeDetectorRef);
  
  // Données
  transactions: any[] = [];
  mesComptes: any[] = [];
  
  // Filtres
  dateDebut: string = '';
  dateFin: string = '';
  typeFiltre: string = 'TOUS';
  
  // État
  loading = false;
  genererLoading = false;
  
  // Types de transaction pour l'affichage
  readonly TYPES_TRANSACTION = {
    DEPOT: 'depot',
    RETRAIT: 'retrait',
    VIREMENT_EMIS: 'virement-emis',
    VIREMENT_RECU: 'virement-recus',
    AUTRE: 'autre'
  };
  
  ngOnInit(): void {
    this.chargerDonnees();
  }
  
  private chargerDonnees(): void {
    this.loading = true;
    
    const user = this.loginService.user();
    if (!user?.clientId) {
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }
    
    this.dashboardService.getComptesClient(user.clientId).subscribe({
      next: (comptes) => {
        this.mesComptes = comptes;
        this.chargerTransactions(user.clientId);
      },
      error: (error) => {
        console.error('Erreur chargement comptes:', error);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
  
  private chargerTransactions(clientId: number): void {
    this.dashboardService.getTransactionsClient(clientId).subscribe({
      next: (transactions) => {
        this.transactions = transactions;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur chargement transactions:', error);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
  
  // ============ FILTRES ============
  
  get transactionsFiltrees(): any[] {
    let filtered = [...this.transactions];
    
    // Filtre par date début
    if (this.dateDebut) {
      const dateDebut = new Date(this.dateDebut);
      filtered = filtered.filter(t => new Date(t.date) >= dateDebut);
    }
    
    // Filtre par date fin
    if (this.dateFin) {
      const dateFin = new Date(this.dateFin);
      dateFin.setHours(23, 59, 59);
      filtered = filtered.filter(t => new Date(t.date) <= dateFin);
    }
    
    // Filtre par type
    if (this.typeFiltre !== 'TOUS') {
      filtered = filtered.filter(t => t.type === this.typeFiltre);
    }
    
    // Tri par date décroissante
    return filtered.sort((a, b) => 
      new Date(b.date).getTime() - new Date(a.date).getTime()
    );
  }
  
  reinitialiserFiltres(): void {
    this.dateDebut = '';
    this.dateFin = '';
    this.typeFiltre = 'TOUS';
    this.cdr.detectChanges();
  }
  
  appliquerFiltres(): void {
    this.cdr.detectChanges();
  }
  
  // ============ UTILITAIRES COMPTES ============
  
  estMonCompte(numeroCompte: string): boolean {
    if (!numeroCompte) return false;
    
    return this.mesComptes.some(compte => 
      compte.numero === numeroCompte || 
      compte.numeroCompte === numeroCompte
    );
  }
  
  // ============ UTILITAIRES TRANSACTIONS ============
  
  getTypeTransaction(transaction: any): string {
    if (!transaction?.type) return this.TYPES_TRANSACTION.AUTRE;
    
    switch(transaction.type) {
      case 'DEPOT':
        return this.TYPES_TRANSACTION.DEPOT;
        
      case 'RETRAIT':
        return this.TYPES_TRANSACTION.RETRAIT;
        
      case 'VIREMENT':
        if (this.estMonCompte(transaction.numeroCompteSource)) {
          return this.TYPES_TRANSACTION.VIREMENT_EMIS;
        }
        if (this.estMonCompte(transaction.numeroCompteDestination)) {
          return this.TYPES_TRANSACTION.VIREMENT_RECU;
        }
        return this.TYPES_TRANSACTION.AUTRE;
        
      default:
        return this.TYPES_TRANSACTION.AUTRE;
    }
  }
  
  getMessageTransaction(transaction: any): string {
    const type = this.getTypeTransaction(transaction);
    
    switch(type) {
      case this.TYPES_TRANSACTION.DEPOT:
        return 'Vous avez effectué un dépôt';
        
      case this.TYPES_TRANSACTION.RETRAIT:
        return 'Vous avez effectué un retrait';
        
      case this.TYPES_TRANSACTION.VIREMENT_EMIS:
        const dest = transaction.proprietaireDestination || 'compte externe';
        return `Vous avez transféré à ${dest}`;
        
      case this.TYPES_TRANSACTION.VIREMENT_RECU:
        const source = transaction.proprietaireSource || 'compte externe';
        return `Vous avez reçu de ${source}`;
        
      default:
        return 'Transaction effectuée';
    }
  }
  
  getIconeTransaction(type: string): string {
    switch(type) {
      case this.TYPES_TRANSACTION.DEPOT:
        return 'bi-arrow-down-circle text-success';
      case this.TYPES_TRANSACTION.RETRAIT:
        return 'bi-arrow-up-circle text-danger';
      case this.TYPES_TRANSACTION.VIREMENT_EMIS:
        return 'bi-send text-warning';
      case this.TYPES_TRANSACTION.VIREMENT_RECU:
        return 'bi-receipt text-info';
      default:
        return 'bi-arrow-left-right';
    }
  }
  
  // ============ GÉNÉRATION DE RELEVÉ ============
  
  genererReleveGlobal(): void {
    // Validation
    if (!this.validerGenerationReleve()) return;
    
    // Préparation des dates
    const { dateDebutISO, dateFinISO } = this.preparerDatesReleve();
    const user = this.loginService.user()!;
    
    console.log('📄 Génération relevé GLOBAL:', {
      clientId: user.clientId,
      dateDebut: dateDebutISO,
      dateFin: dateFinISO,
      nbTransactions: this.transactionsFiltrees.length
    });
    
    // Appel API
    this.genererLoading = true;
    
    this.transactionService.telechargerReleveGlobal(
      user.clientId,
      dateDebutISO,
      dateFinISO
    ).subscribe({
      next: (pdfBlob) => this.onReleveSuccess(pdfBlob, user.clientId),
      error: (error) => this.onReleveError(error)
    });
  }
  
  private validerGenerationReleve(): boolean {
    const user = this.loginService.user();
    
    if (!user?.clientId) {
      alert('Veuillez vous connecter pour générer un relevé');
      return false;
    }
    
    if (this.mesComptes.length === 0) {
      alert('Aucun compte trouvé pour générer le relevé');
      return false;
    }
    
    return true;
  }
  
  private preparerDatesReleve(): { dateDebutISO: string, dateFinISO: string } {
    let dateDebutISO: string;
    let dateFinISO: string;
    
    // Date de début
    if (this.dateDebut) {
      const dateDebutObj = new Date(this.dateDebut);
      dateDebutObj.setHours(0, 0, 0, 0);
      dateDebutISO = dateDebutObj.toISOString().slice(0, 19);
    } else {
      // Par défaut: il y a 30 jours
      const date = new Date();
      date.setDate(date.getDate() - 30);
      date.setHours(0, 0, 0, 0);
      dateDebutISO = date.toISOString().slice(0, 19);
    }
    
    // Date de fin
    if (this.dateFin) {
      const dateFinObj = new Date(this.dateFin);
      dateFinObj.setHours(23, 59, 59, 999);
      dateFinISO = dateFinObj.toISOString().slice(0, 19);
    } else {
      // Par défaut: aujourd'hui
      const date = new Date();
      date.setHours(23, 59, 59, 999);
      dateFinISO = date.toISOString().slice(0, 19);
    }
    
    return { dateDebutISO, dateFinISO };
  }
  
  private onReleveSuccess(pdfBlob: Blob, clientId: number): void {
    this.genererLoading = false;
    
    // Vérifier que le blob n'est pas vide
    if (pdfBlob.size === 0) {
      alert('Le relevé généré est vide');
      return;
    }
    
    // Télécharger le fichier
    this.telechargerPDFGlobal(pdfBlob, clientId);
    
    // Feedback utilisateur
    const nbTransactions = this.transactionsFiltrees.length;
    console.log(`✅ Relevé généré avec ${nbTransactions} transactions`);
  }
  
  private onReleveError(error: any): void {
    this.genererLoading = false;
    console.error('❌ Erreur génération relevé:', error);
    
    let message = 'Erreur lors de la génération du relevé';
    
    if (error.status === 404) {
      message = 'Aucun relevé trouvé pour cette période';
    } else if (error.status === 400) {
      message = 'Paramètres de période invalides';
    } else if (error.status === 403) {
      message = 'Vous n\'avez pas les permissions nécessaires';
    } else if (error.status === 500) {
      message = 'Erreur serveur, veuillez réessayer plus tard';
    }
    
    alert(message);
  }
  
  private telechargerPDFGlobal(blob: Blob, clientId: number): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.style.display = 'none';
    
    // Nom du fichier
    const date = new Date().toISOString().split('T')[0];
    const nomFichier = `releve-global-client-${clientId}-${date}.pdf`;
    
    a.download = nomFichier;
    a.href = url;
    
    // Déclencher le téléchargement
    document.body.appendChild(a);
    a.click();
    
    // Nettoyage
    setTimeout(() => {
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      alert('Relevé téléchargé avec succès !');
    }, 100);
  }
  
  // ============ MÉTHODES UTILITAIRES ============
  
  formaterDate(dateString: string): string {
    if (!dateString) return '';
    
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
  
  formaterMontant(montant: number): string {
    return new Intl.NumberFormat('fr-FR', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(montant) + ' FCFA';
  }
  
  getPeriodeAffichage(): string {
    if (this.dateDebut || this.dateFin) {
      const debut = this.dateDebut || 'Début non spécifié';
      const fin = this.dateFin || 'Fin non spécifié';
      return `${debut} au ${fin}`;
    }
    return '30 derniers jours';
  }
  
  getNombreComptes(): number {
    return this.mesComptes.length;
  }
  
  getNombreTransactionsFiltrees(): number {
    return this.transactionsFiltrees.length;
  }

  voirReleve(): void {
    if (!this.validerGenerationReleve()) return;
    
    const { dateDebutISO, dateFinISO } = this.preparerDatesReleve();
    const user = this.loginService.user()!;
    
    this.genererLoading = true;
    
    this.transactionService.telechargerReleveGlobal(
      user.clientId,
      dateDebutISO,
      dateFinISO
    ).subscribe({
      next: (pdfBlob) => {
        this.genererLoading = false;
        
        // Ouvrir dans nouvel onglet
        const blobUrl = URL.createObjectURL(pdfBlob);
        window.open(blobUrl, '_blank');
        
        // Le navigateur montrera son bouton de télécharger en bas
      },
      error: (error) => this.onReleveError(error)
    });
  }

}