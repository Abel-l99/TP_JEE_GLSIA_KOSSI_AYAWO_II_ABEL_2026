import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../services/dashboard-service';
import { Router } from '@angular/router';
import { Clients } from '../clients/clients';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard {
  
  dashboardService = inject(DashboardService);
  router = inject(Router);
  cdr = inject(ChangeDetectorRef);

  counts: number[] = [0, 0, 0];
  totalSolde: number = 0;
  
  constructor() {
    this.dashboardService.getAllCounts().subscribe(([clients, comptes, transactions]) => {
      this.counts = [
        (clients as any).count,
        (comptes as any).count,
        (transactions as any).count
      ];
      this.cdr.detectChanges();
    });

    this.dashboardService.getTotalSolde().subscribe(response => {
      this.totalSolde = (response as any).total;
      this.cdr.detectChanges();
    });
  }

  ngOnInit() {
    this.dashboardService.getTotalSolde().subscribe(({total}) => {
      this.totalSolde = total;
      this.cdr.detectChanges();
    });
  }
  
}