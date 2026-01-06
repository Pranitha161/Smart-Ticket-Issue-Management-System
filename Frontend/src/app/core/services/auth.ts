import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LookupService } from './lookup-service';
import { Router, RouterLink } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.auth}`;

  // Signals
  loggedIn = signal(false);
  username = signal<string | null>(null);
  roles = signal<string[]>([]);
  userId = signal<string | null>(null);
  email=   signal<string | null>(null);

  constructor(private http: HttpClient,
    private lookup:LookupService,
    private router:Router
  ) {
    const token = this.getToken();
    if (token) {
      this.loggedIn.set(true);
      this.username.set(this.extractUsername(token));
      this.roles.set(this.extractRoles(token));
      this.userId.set(this.extractUserId(token)); 
      this.email.set(this.extractUserEmail(token));  
    }
  }

  signup(user: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/register`, user);
  }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/login`, credentials);
  }

  create(user: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/create`, user);
  }

  setToken(token: string): void {
    localStorage.setItem('token', token);
    this.loggedIn.set(true);
    this.username.set(this.extractUsername(token));
    this.roles.set(this.extractRoles(token));
    this.userId.set(this.extractUserId(token));  
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  logout(): void {
    localStorage.removeItem('token');
    this.loggedIn.set(false);
    this.username.set(null);
    this.roles.set([]);
    this.userId.set(null); 
    this.lookup.clear(); 
    this.router.navigate(['/auth/login']);
    console.log('🚪 User logged out and cache cleared');
  }

  private extractRoles(token: string): string[] {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.roles || [];
    } catch {
      return [];
    }
  }

  private extractUsername(token: string): string | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.username || payload.sub || null;
    } catch {
      return null;
    }
  }
  private extractUserEmail(token: string): string | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.email || payload.sub || null;
    } catch {
      return null;
    }
  }

  private extractUserId(token: string): string | null {   
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.sub || payload.userId || null;
    } catch {
      return null;
    }
  }
}
