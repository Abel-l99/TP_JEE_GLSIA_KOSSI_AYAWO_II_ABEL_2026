import { Component } from '@angular/core';
import { AdminHeader } from "../admin-header/admin-header";
import { RouterOutlet } from "@angular/router";
import { AdminSidebar } from "../admin-sidebar/admin-sidebar";

@Component({
  selector: 'app-admin-layout',
  imports: [AdminHeader, RouterOutlet, AdminSidebar],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.scss',
})
export class AdminLayout {

}
