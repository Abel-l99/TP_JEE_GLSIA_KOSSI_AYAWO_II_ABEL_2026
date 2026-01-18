import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { LoginService } from '../../../services/login/login-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-sidebar',
  imports: [RouterLink, CommonModule],
  templateUrl: './admin-sidebar.html',
  styleUrl: './admin-sidebar.scss',
})
export class AdminSidebar {

  private loginService = inject(LoginService);
  private router = inject(Router);

  showConfirm = false;

  openConfirm() {
    this.showConfirm = true;
  }

  closeConfirm() {
    this.showConfirm = false;
  }

  confirmLogout() {
    this.loginService.logout();
    this.router.navigate(['/login']);
    this.showConfirm = false;
  }
  
}
