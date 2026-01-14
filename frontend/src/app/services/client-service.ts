import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

const BASE_URL = 'http://localhost:8080';

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  http = inject(HttpClient);
  
  private getHeaders() {
    return new HttpHeaders({
      "Authorization": "Bearer " + localStorage.getItem("token")
    });
  }

  createClient(clientData: any) {
    return this.http.post(BASE_URL + '/client', clientData, 
      { headers: this.getHeaders() });
  }

  getClientById(id: number) {
    return this.http.get(BASE_URL + `/client/${id}`, 
      { headers: this.getHeaders() });
  }

}