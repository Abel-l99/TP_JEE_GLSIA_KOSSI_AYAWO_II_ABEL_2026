import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardService } from '../../services/dashboard-service';
import { FormsModule } from '@angular/forms'; // AJOUTER
import { CommonModule } from '@angular/common'; // AJOUTER

@Component({
  selector: 'app-clients',
  imports: [RouterLink, FormsModule, CommonModule], // AJOUTER FormsModule et CommonModule
  templateUrl: './clients.html',
  styleUrl: './clients.scss',
})
export class Clients implements OnInit {

  dashboardService = inject(DashboardService);
  cdr = inject(ChangeDetectorRef);

  clients: any[] = [];
  clientsFiltres: any[] = []; // AJOUTER
  recherche: string = ''; // AJOUTER
  loading: boolean = false; // AJOUTER (optionnel)

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients() {
    this.loading = true; // AJOUTER (optionnel)
    this.dashboardService.getAllClients().subscribe(clients => {
      this.clients = clients;
      this.clientsFiltres = [...clients]; // AJOUTER
      console.log(this.clients);
      this.loading = false; // AJOUTER (optionnel)
      this.cdr.detectChanges();
    });
  }

  // AJOUTER CES 3 MÉTHODES :
  filtrerClients(): void {
    if (!this.recherche.trim()) {
      this.clientsFiltres = [...this.clients];
      return;
    }

    const terme = this.recherche.toLowerCase().trim();
    
    this.clientsFiltres = this.clients.filter(client => 
      (client.nom && client.nom.toLowerCase().includes(terme)) ||
      (client.prenom && client.prenom.toLowerCase().includes(terme)) ||
      (client.email && client.email.toLowerCase().includes(terme)) ||
      ((client.nom + ' ' + client.prenom).toLowerCase().includes(terme))
    );
  }

  onRechercheChange(): void {
    this.filtrerClients();
  }

  reinitialiserRecherche(): void {
    this.recherche = '';
    this.filtrerClients();
  }
}