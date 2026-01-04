import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SlaRuleModel } from '../../shared/models/sla-rule.model'; 
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SlaRule {
  private  baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.assignments}/sla-rules`;

  constructor(private http: HttpClient) {}

  getAllRules(options: any = {}): Observable<SlaRuleModel[]> {
    return this.http.get<SlaRuleModel[]>(this.baseUrl);
  }

  addRule(rule: SlaRuleModel): Observable<SlaRuleModel> {
    return this.http.post<SlaRuleModel>(this.baseUrl, rule);
  }

  updateRule(id: string, rule: SlaRuleModel): Observable<SlaRuleModel> {
    return this.http.put<SlaRuleModel>(`${this.baseUrl}/${id}`, rule);
  }

  deleteRule(id: string): Observable<SlaRuleModel> {
    return this.http.delete<SlaRuleModel>(`${this.baseUrl}/${id}`);
  }
}
