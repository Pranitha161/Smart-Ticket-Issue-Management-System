import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SlaRuleModel } from '../../shared/models/sla-rule.model'; 

@Injectable({
  providedIn: 'root'
})
export class SlaRule {
  private  baseUrl = 'http://localhost:8765/assignment-escalation-service/sla-rules';

  constructor(private http: HttpClient) {}

  // ✅ Get all SLA rules
  getAllRules(): Observable<SlaRuleModel[]> {
    return this.http.get<SlaRuleModel[]>(this.baseUrl);
  }

  // ✅ Add a new SLA rule
  addRule(rule: SlaRuleModel): Observable<SlaRuleModel> {
    return this.http.post<SlaRuleModel>(this.baseUrl, rule);
  }

  // ✅ Update an SLA rule by ID
  updateRule(id: string, rule: SlaRuleModel): Observable<SlaRuleModel> {
    return this.http.put<SlaRuleModel>(`${this.baseUrl}/${id}`, rule);
  }

  // ✅ Delete an SLA rule by ID
  deleteRule(id: string): Observable<SlaRuleModel> {
    return this.http.delete<SlaRuleModel>(`${this.baseUrl}/${id}`);
  }
}
