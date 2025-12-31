export interface SlaRuleModel {
  id?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  responseMinutes: number;
  resolutionMinutes: number;
}
