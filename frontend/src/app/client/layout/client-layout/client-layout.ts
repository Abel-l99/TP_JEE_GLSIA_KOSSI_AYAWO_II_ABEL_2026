import { Component } from '@angular/core';
import { RouterOutlet } from "@angular/router";
import { ClientHeader } from "../client-header/client-header";
import { ClientSidebar } from '../client-sidebar/client-sidebar';

@Component({
  selector: 'app-client-layout',
  imports: [RouterOutlet, ClientHeader, ClientSidebar],
  templateUrl: './client-layout.html',
  styleUrl: './client-layout.scss',
})
export class ClientLayout {

}
