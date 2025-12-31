import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface User {
  id: string;
  displayId: string;
  email: string;
  password: string;
  username: string;
  enabled: boolean;
  roles: string[];
}
export interface CreateUserRequest {
  username: string;
  email: string;
  password: string;
  roles: string[];
}
export interface UserStatsDto {
  totalUsers: number;
  activeUsers: number;
  supportAgents: number;
  endUsers: number;
  managers: number;
  admins: number;
}


@Injectable({
  providedIn: 'root'
})
export class AdminUser {
  private baseUrl = 'http://localhost:8765/auth-user-service';

  constructor(private http: HttpClient) { }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/users`);
  }

  getUserById(id: string): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/users/${id}`);
  }

  createUser(user: CreateUserRequest): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/auth/register`, user);
  }

  updateUser(id: string, user: User): Observable<any> {
    return this.http.put(`${this.baseUrl}/users/${id}`, user);
  }

  deleteUser(id: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/auth/${id}`);
  }

  getUserStats(): Observable<UserStatsDto> {
    return this.http.get<UserStatsDto>(`${this.baseUrl}/auth/users/stats`);
  }
}
