// services/dashboard.service.ts
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { count, forkJoin, Observable } from 'rxjs';


const BASE_URL = 'http://localhost:8080';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  http = inject(HttpClient);

  constructor(){}

  getAllCounts() {
    const headers = new HttpHeaders({
      "Authorization": "Bearer " + localStorage.getItem("token")
    });
    
    const requests = [
      this.http.get(BASE_URL + '/client/count', { headers }),
      this.http.get(BASE_URL + '/compte/count', { headers }),
      this.http.get(BASE_URL + '/transaction/count', { headers })
    ];
    
    return forkJoin(requests);
  }

  getTotalSolde() {
    const headers = new HttpHeaders({
      "Authorization": "Bearer " + localStorage.getItem("token")
    });
    
    return this.http.get<{total: number}>(BASE_URL + '/compte/total-solde', { headers });
  }

  getAllClients() {
    const headers = new HttpHeaders({
      "Authorization": "Bearer " + localStorage.getItem("token")
    });
    
    return this.http.get<any[]>(BASE_URL + '/client', { headers });
  }

  getComptesSummary() {
    const headers = new HttpHeaders({
      "Authorization": "Bearer " + localStorage.getItem("token")
    });
    
    return this.http.get<any[]>(BASE_URL + '/compte/summary', { headers });
  }

  getComptesClient(clientId: number): Observable<any[]> {
    const headers = new HttpHeaders({
      "Authorization": "Bearer " + localStorage.getItem("token")
    });
    return this.http.get<any[]>(`${BASE_URL}/compte/client/${clientId}`, { headers });
  }

}