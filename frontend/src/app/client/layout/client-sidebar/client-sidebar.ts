import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { LoginService } from '../../../services/login/login-service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-client-sidebar',
  imports: [RouterLink, CommonModule],
  templateUrl: './client-sidebar.html',
  styleUrl: './client-sidebar.scss',
})
export class ClientSidebar {

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
