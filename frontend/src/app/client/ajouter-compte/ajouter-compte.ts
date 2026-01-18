import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { ClientService } from '../../services/client-service';
import { LoginService } from '../../services/login/login-service';

@Component({
  selector: 'app-ajouter-compte',
  imports: [CommonModule, FormsModule],
  templateUrl: './ajouter-compte.html',
  styleUrl: './ajouter-compte.scss',
})
export class AjouterCompte implements OnInit {
  
  private clientService = inject(ClientService);
  private loginService = inject(LoginService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  
  // Données du formulaire
  compteData = {
    typeCompte: 'COURANT',
    soldeInitial: 0,
    clientId: 0
  };
  
  // États
  loading = false;
  message = '';
  isError = false;
  showConfirmation = false; 
  typesCompte = ['COURANT', 'EPARGNE'];
  
  // Client info (pour affichage)
  clientInfo: any = null;

  ngOnInit(): void {
    // RÉCUPÉRER L'ID DEPUIS L'URL SEULEMENT
    this.route.params.subscribe(params => {
      const clientIdFromUrl = params['id'];
      
      if (clientIdFromUrl) {
        // UTILISER L'ID DE L'URL
        this.compteData.clientId = +clientIdFromUrl; // + pour convertir en number
        console.log('🔗 ID Client depuis URL:', this.compteData.clientId);
        
        // Charger les infos du client pour affichage (optionnel)
        this.chargerClientInfo(this.compteData.clientId);
      } else {
        // Si pas d'ID dans l'URL, erreur
        this.isError = true;
        this.message = 'ID client manquant dans l\'URL';
        console.error('❌ Aucun ID client dans l\'URL');
      }
    });
  }

  // Charger les infos du client (optionnel, pour affichage)
  private chargerClientInfo(clientId: number): void {
    this.clientService.getClientById(clientId).subscribe({
      next: (client) => {
        this.clientInfo = client;
        console.log('✅ Client trouvé:', client);
      },
      error: (error) => {
        console.error('❌ Erreur chargement client:', error);
        this.clientInfo = { nom: 'Client inconnu', prenom: '' };
      }
    });
  }

  // Étape 1: Préparation
  preparerCreation(): void {
    if (!this.isFormValid()) {
      this.message = 'Veuillez remplir tous les champs';
      this.isError = true;
      return;
    }
    
    if (this.compteData.clientId === 0) {
      this.message = 'ID client invalide';
      this.isError = true;
      return;
    }
    
    // Affiche la boîte de confirmation
    this.showConfirmation = true;
  }

  // Étape 2: Confirmation
  confirmerCreation(): void {
    this.showConfirmation = false;
    this.creerCompte();
  }

  // Étape 3: Annulation
  annulerCreation(): void {
    this.showConfirmation = false;
  }

  // Étape 4: Création réelle
  private creerCompte(): void {
    this.loading = true;
    this.message = '';
    this.isError = false;
    
    console.log('📤 Envoi des données:', this.compteData);
    
    this.clientService.ajouterCompte(this.compteData).subscribe({
      next: (response) => {
        this.loading = false;
        this.isError = false;
        this.message = '✅ Compte créé avec succès pour le client ' + 
                      (this.clientInfo ? `${this.clientInfo.nom} ${this.clientInfo.prenom}` : `ID ${this.compteData.clientId}`) + ' !';
        
        setTimeout(() => {
          // TOUJOURS rediriger vers admin/clients (car c'est l'admin qui crée)
          this.router.navigate(['/admin/clients']);
        }, 2000);
      },
      error: (error) => {
        this.loading = false;
        this.isError = true;
        console.error('❌ Erreur API:', error);
        this.message = error.error?.message || 'Erreur lors de la création du compte';
      }
    });
  }

  isFormValid(): boolean {
    return this.compteData.clientId > 0 && 
           this.compteData.typeCompte.length > 0;
  }

  // Retour à la liste des clients (annuler)
  annuler(): void {
    this.router.navigate(['/admin/clients']);
  }
}