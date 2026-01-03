import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AgentProfile { agentLevel: string; category: string; skills: string[]; currentAssignments: number; }
export interface User {
  id: string;
  displayId: string;
  email: string;
  password: string;
  username: string;
  enabled: boolean;
  roles: string[];
  agentProfile?: AgentProfile;
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
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.auth}`;

  constructor(private http: HttpClient) { }

  getAllUsers(options: any = {}): Observable<User[]> {
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

  getAgentsByCategory(categoryId: string): Observable<any[]> {
  return this.http.get<any[]>(`${this.baseUrl}/agents?category=${categoryId}`);
}
  changePassword(userName: string, oldPassword: string, newPassword: string): Observable<any> {
  return this.http.post<any>(
    `${this.baseUrl}/auth/change-password`,
    null, 
    {
      params: {
        userName: userName,
        oldPassword: oldPassword,
        newPassword: newPassword
      }
    }
  );
}

requestPasswordReset(email: string): Observable<any> {
  return this.http.post<any>(
    `${this.baseUrl}/auth/request-reset`,
    null,
    { params: { email } }
  );
}

resetPassword(token: string, newPassword: string): Observable<any> {
  return this.http.post<any>(
    `${this.baseUrl}/auth/reset-password`,
    null,
    { params: { token, newPassword } }
  );
}


}
