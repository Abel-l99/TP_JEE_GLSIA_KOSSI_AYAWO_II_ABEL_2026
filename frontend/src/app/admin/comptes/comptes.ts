import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { DashboardService } from '../../services/dashboard-service';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-comptes',
  imports: [DatePipe],
  templateUrl: './comptes.html',
  styleUrl: './comptes.scss',
})
export class Comptes {

  dashboardService = inject(DashboardService);
  cdr = inject(ChangeDetectorRef);

  comptes: any[] = [];

  ngOnInit(): void {
    this.loadComptes();
  }

  loadComptes() {
    this.dashboardService.getComptesSummary().subscribe(comptes => {
      this.comptes = comptes;
      console.log(this.comptes);
      this.cdr.detectChanges();
    });
  }
  
}
