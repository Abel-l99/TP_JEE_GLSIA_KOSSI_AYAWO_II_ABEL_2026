import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { User } from '../../models/user.model';
import { Credentials } from '../../models/credentials.model';
import { Observable, catchError, map, switchMap, tap, throwError, of } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  private http = inject(HttpClient);
  private readonly BASE_URL = 'http://localhost:8080';

  user = signal<User | null | undefined>(undefined);

  constructor() {
    this.initializeUser();
  }

  private initializeUser(): void {
    if (typeof localStorage === 'undefined') {
      this.user.set(null);
      return;
    }

    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');

    if (token && username) {
      // Charge l'utilisateur depuis l'API
      this.getUserByUsername(username).subscribe({
        error: () => {
          // En cas d'erreur (token expiré, etc.)
          this.clearAuth();
        }
      });
    } else {
      this.user.set(null);
    }
  }

  login(credentials: Credentials): Observable<User> {
    console.log('🔐 Login attempt for:', credentials.username);
    
    return this.http.post<{ token: string }>(
      `${this.BASE_URL}/auth/login`, 
      credentials
    ).pipe(
      tap(response => {
        // Stocke le token ET le username
        localStorage.setItem('token', response.token);
        localStorage.setItem('username', credentials.username);
      }),
      switchMap(() => this.getUserByUsername(credentials.username)),
      catchError(error => {
        this.clearAuth();
        return throwError(() => error);
      })
    );
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.BASE_URL}/user`, userData);
  }

  getUserByUsername(username: string): Observable<User> {
    console.log('🔍 Fetching user:', username);
    
    return this.http.get<any>(`${this.BASE_URL}/user/username/${username}`).pipe(
      // Gère le cas où l'API retourne un tableau
      map(response => {
        if (Array.isArray(response)) {
          return response[0];
        }
        return response;
      }),
      tap(user => {
        this.user.set(user);
      }),
      catchError(error => {
        this.user.set(null);
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.user.set(null);
    
    window.location.href = '/login';
  }

  private clearAuth(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    this.user.set(null);
  }

  isAuthenticated(): boolean {
    return !!this.user() && !!localStorage.getItem('token');
  }

}