import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DashboardService } from '../../services/dashboard-service';

@Component({
  selector: 'app-clients',
  imports: [RouterLink],
  templateUrl: './clients.html',
  styleUrl: './clients.scss',
})
export class Clients implements OnInit {

  dashboardService = inject(DashboardService);
  cdr = inject(ChangeDetectorRef);

  clients: any[] = [];

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients() {
    this.dashboardService.getAllClients().subscribe(clients => {
      this.clients = clients;
      console.log(this.clients);
      this.cdr.detectChanges();
    });
  }


}
