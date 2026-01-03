import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth';
import { AssignmentEscalation } from '../../../core/services/assignment-escalation';
import { AdminUser } from '../../../core/services/admin-user';
import { LookupService } from '../../../core/services/lookup-service'; 
import { ticketListService } from '../../../core/services/ticket-list'; 

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './ticket-list.html',
  styleUrls: ['./ticket-list.css']
})
export class TicketList implements OnInit {
  tickets: any[] = [];           
  filteredTickets: any[] = [];   
  pagedTickets: any[] = [];      
  searchTerm = '';
  selectedPriority = '';
  selectedStatus = '';
  currentPage = 0;
  pageSize = 5;
  totalPages = 0;
  role: string = ''; 
  assigningTicketId: string | null = null;
  agentsByCategory: { [categoryId: string]: any[] } = {};

  constructor(
    private ticketListService: ticketListService,   
    private assignmentService: AssignmentEscalation, 
    private authService: AuthService,
    private userAdminService: AdminUser,
    private lookup: LookupService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
  this.role = this.authService.roles()[0];
  const userId = this.authService.userId()!;

  if (this.role === 'USER') {
    this.ticketListService.getTicketsByUserId(userId).subscribe({
      next: (res) => this.initTickets(res),
      error: () => console.error('Failed to load tickets for user')
    });
  } else if (this.role === 'AGENT') {
    this.ticketListService.getTicketsByAgentId(userId).subscribe({
      next: (res) => this.initTickets(res),
      error: () => console.error('Failed to load tickets for agent')
    });
  } else {
    this.ticketListService.getTickets().subscribe({
      next: (res) => {this.initTickets(res);console.log(res);},
      error: () => console.error('Failed to load tickets')
    });
  }
}
private initTickets(res: any[]): void {
  this.tickets = res;
  this.filteredTickets = res;
  this.totalPages = Math.ceil(this.filteredTickets.length / this.pageSize);
  this.setPage(0);
  this.cd.detectChanges();
}


  private safeIncludes(field: string | null | undefined, term: string): boolean {
    return field ? field.toLowerCase().includes(term) : false;
  }

  applyFilters(): void {
    const term = this.searchTerm.toLowerCase().trim();

    this.filteredTickets = this.tickets.filter(ticket => {
      const matchesSearch =
        !term || 
        this.safeIncludes(ticket.title, term) ||
        this.safeIncludes(ticket.displayId, term) ||
        this.safeIncludes(ticket.categoryName, term); 

      const matchesPriority =
        !this.selectedPriority || ticket.priority === this.selectedPriority;
      const matchesStatus =
        !this.selectedStatus || ticket.status === this.selectedStatus;

      return matchesSearch && matchesPriority && matchesStatus;
    });

    this.totalPages = Math.ceil(this.filteredTickets.length / this.pageSize);
    this.setPage(0);
  }

  loadAgentsForCategory(categoryId: string): void {
    this.userAdminService.getAgentsByCategory(categoryId).subscribe({
      next: (agents) => {
        this.agentsByCategory[categoryId] = agents;
        this.cd.detectChanges();
      },
      error: () => console.error(`Failed to load agents for category ${categoryId}`)
    });
  }

  setPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      const start = page * this.pageSize;
      const end = start + this.pageSize;
      this.pagedTickets = this.filteredTickets.slice(start, end);
    } else {
      this.pagedTickets = [];
    }
  }

  manualAssign(ticket: any) {
    this.assigningTicketId = ticket.id;
    this.loadAgentsForCategory(ticket.categoryId);
  }

  confirmAssign(ticketId: string, agentId: string) {
    if (agentId) {
      this.assignmentService.manualAssign(ticketId, agentId, 'MEDIUM') 
        .subscribe(res => {
          alert(res.message);
          console.log(res);
        });
      this.assigningTicketId = null; 
    }
  }

  autoAssign(ticket: any) {
    this.assignmentService.autoAssign(ticket.id)
      .subscribe(res => {
        alert(`Ticket ${res.ticketId} auto-assigned to agent ${res.agentId}`);
      });
  }
}
