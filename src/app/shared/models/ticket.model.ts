export interface UserTicketStats { total: number; open: number; resolved: number; critical: number; }
export interface StatusSummaryDto { status: string; count: number; }
export interface PrioritySummaryDto { priority: string; count: number; }
export interface Ticket {
  id: string;   
  displayId: string;           
  title: string;           
  description?: string;    
  status: string;   
  categoryId: string;       
  priority: string;        
  createdBy: string;       
  assignedTo?: string;     
  createdAt: string;    
  updatedDate?: string;    
}
