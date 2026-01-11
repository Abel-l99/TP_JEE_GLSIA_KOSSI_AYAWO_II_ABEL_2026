import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { LoginService } from '../../../services/login/login-service';

@Component({
  selector: 'app-admin-sidebar',
  imports: [RouterLink],
  templateUrl: './admin-sidebar.html',
  styleUrl: './admin-sidebar.scss',
})
export class AdminSidebar {

  private loginService = inject(LoginService);
  private router = inject(Router);

  logout() {
    this.loginService.logout();
    this.router.navigate(['/login']);
  }
}