import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateUserRequest, User, UserStatsDto } from '../../shared/models/authuser.model';
import { AgentStatsDto } from '../../shared/models/dashboard.model';
import { ApiResponse } from '../../shared/models/api-response.model';

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
  getAgentSummaryStats(agentId: string): Observable<AgentStatsDto> {
    return this.http.get<AgentStatsDto>(`${this.baseUrl}/${agentId}/stats`);
  }

  getAllAgentStats(): Observable<AgentStatsDto[]> {
    return this.http.get<AgentStatsDto[]>(`${this.baseUrl}/stats`);
  }

  enableUser(id: string): Observable<ApiResponse> {
    return this.http.put<ApiResponse>(`${this.baseUrl}/${id}/enable`, {});

  }
}
