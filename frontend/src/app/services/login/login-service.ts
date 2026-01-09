import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { User } from '../../models/user.model';
import { Credentials } from '../../models/credentials.model';
import { Observable, map, switchMap, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class LoginService {

  private http = inject(HttpClient);
  private readonly BASE_URL = 'http://localhost:8080';

  user = signal<User | null | undefined>(undefined);

  constructor() {
    const token = (typeof localStorage !== 'undefined') ? localStorage.getItem('token') : null;
    if (token) {
      this.getUsers().subscribe();
    } else {
      this.user.set(null);
    }
  }

  login(credentials: Credentials): Observable<User> {
    return this.http.post<{ token: string }>(`${this.BASE_URL}/auth/login`, credentials)
      .pipe(
        tap(response => {
          const token = response.token;
          localStorage.setItem('token', token);
        }),
        switchMap(() => this.getUsers())
      );
  }

  getUsers(): Observable<User> {
    return this.http.get<User[]>(`${this.BASE_URL}/user`).pipe(
      map(users => {
        return Array.isArray(users) ? users[0] : users;
      }),
      tap(user => {
        this.user.set(user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    this.user.set(null);
  }

  isAuthenticated(): boolean {
    return !!this.user();
  }
}

