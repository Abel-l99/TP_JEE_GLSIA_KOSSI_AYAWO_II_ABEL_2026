import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { LoginService } from '../../services/login/login-service';
import { DashboardService } from '../../services/dashboard-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-comptes-client',
  imports: [CommonModule],
  templateUrl: './comptes-client.html',
  styleUrl: './comptes-client.scss',
})
export class ComptesClient implements OnInit {
  
  private loginService = inject(LoginService);
  private dashboardService = inject(DashboardService);
  cdr = inject(ChangeDetectorRef);
  
  comptes: any[] = [];

  ngOnInit(): void {
    // Attends que l'utilisateur soit chargé
    setTimeout(() => {
      const user = this.loginService.user();
      if (user?.clientId) {
        this.dashboardService.getComptesClient(user.clientId).subscribe({
          next: (comptes) => {
            this.comptes = comptes;
            this.cdr.detectChanges(); 
            console.log('Comptes affichés:', this.comptes);
          }
        });
      }
    }, 2000);
  }
}