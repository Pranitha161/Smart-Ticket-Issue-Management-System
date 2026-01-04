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