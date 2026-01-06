import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth';
import { AssignmentEscalation } from '../../../core/services/assignment-escalation';
import { LookupService } from '../../../core/services/lookup-service';
import { ticketListService } from '../../../core/services/ticket-list';
import { Toast } from '../../../core/services/toast';

@Component({
  selector: 'app-ticket-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './ticket-list.html',
  styleUrls: ['./ticket-list.css']
})
export class TicketList implements OnInit {
  private ticketListService = inject(ticketListService);
  private assignmentService = inject(AssignmentEscalation);
  private authService = inject(AuthService);
  private cd = inject(ChangeDetectorRef);
  public lookup = inject(LookupService);
  public toast = inject(Toast);

  tickets: any[] = [];
  filteredTickets: any[] = [];
  pagedTickets: any[] = [];

  searchTerm = '';
  selectedPriority = '';
  selectedStatus = '';
  currentPage = 0;
  pageSize = 6;
  totalPages = 0;
  role: string = '';
  assigningTicketId: string | null = null;
  reopeningTicket: any = null;
  ngOnInit(): void {
    const roles = this.authService.roles();
    this.role = roles && roles.length > 0 ? roles[0] : '';
    this.loadTickets();
  }

  loadTickets(): void {
    this.searchTerm = '';
    this.selectedPriority = '';
    this.selectedStatus = '';

    const userId = this.authService.userId()!;
    const request$ = this.role === 'USER'
      ? this.ticketListService.getTicketsByUserId(userId)
      : this.role === 'AGENT'
        ? this.ticketListService.getTicketsByAgentId(userId)
        : this.ticketListService.getTickets();

    request$.subscribe({
      next: (res) => {
        this.tickets = res;
        this.filteredTickets = [...this.tickets];
        this.totalPages = Math.ceil(this.filteredTickets.length / this.pageSize);
        if (this.filteredTickets.length > 0) {
          this.setPage(0);
        } else {
          this.pagedTickets = [];
        }
        this.cd.detectChanges();
      },
      error: () => this.toast.show('Failed to load tickets', 'error')
    });
  }
  openReopenConfirm(ticket: any) {
    this.reopeningTicket = ticket;
  }

  confirmReopen() {
    if (!this.reopeningTicket) return;

    this.ticketListService.reopenTicket(this.reopeningTicket.id).subscribe({
      next: () => {
        this.toast.show('Ticket reopened successfully', 'success');
        this.reopeningTicket = null;
        this.loadTickets();
      },
      error: (err) => {
        let displayMessage = 'Failed to reopen ticket';
        if (err.error?.message) {
          const match = err.error.message.match(/"message":"([^"]+)"/);
          displayMessage = (match && match[1]) ? match[1] : err.error.message;
        }
        this.toast.show(displayMessage, 'error');
        this.reopeningTicket = null;
        console.error(err);
      }
    });
  }


  applyFilters(): void {
    const term = this.searchTerm.toLowerCase().trim();

    this.filteredTickets = this.tickets.filter(ticket => {
      const categoryName = (this.lookup.getCategoryName(ticket.categoryId) || '').toLowerCase();
      const creatorName = (this.lookup.getUserName(ticket.createdBy) || '').toLowerCase();
      const title = (ticket.title || '').toLowerCase();
      const displayId = (ticket.displayId || '').toLowerCase();

      const matchesSearch = !term ||
        title.includes(term) ||
        displayId.includes(term) ||
        categoryName.includes(term) ||
        creatorName.includes(term);

      const ticketPriority = (ticket.priority || '').toUpperCase();
      const selectedPri = this.selectedPriority.toUpperCase();
      const matchesPriority = !this.selectedPriority || ticketPriority === selectedPri;

      const ticketStatus = (ticket.status || '').toUpperCase();
      const selectedStat = this.selectedStatus.toUpperCase();
      const matchesStatus = !this.selectedStatus || ticketStatus === selectedStat;

      const result = matchesSearch && matchesPriority && matchesStatus;


      return result;
    });

    this.totalPages = Math.ceil(this.filteredTickets.length / this.pageSize);

    if (this.filteredTickets.length === 0) {
      this.pagedTickets = [];
      this.currentPage = 0;
    } else {
      this.setPage(0);
    }

    this.cd.detectChanges();
  }
  setPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      const start = page * this.pageSize;
      this.pagedTickets = this.filteredTickets.slice(start, start + this.pageSize);
    }
  }

  manualAssign(ticket: any) {
    this.assigningTicketId = ticket.id;
  }

  getAgentsForCurrentCategory(): any[] {
    const ticket = this.tickets.find(t => t.id === this.assigningTicketId);

    if (!ticket) return [];

    return this.lookup.getUserList().filter((u: any) => {
      const isAgent = u.roles && u.roles.includes('AGENT');

      return isAgent && u.agentProfile.categoryId === ticket.categoryId;

    });
  }

  confirmAssign(ticketId: string, agentId: string) {
    console.log('yes');
    const ticket = this.tickets.find(t => t.id === ticketId);
    if (!agentId) return this.toast.show('Select an agent first', 'error');
    this.assignmentService.manualAssign(ticketId, agentId, ticket.priority, ticket.version).subscribe({
      next: () => {
        this.toast.show('Ticket Assigned Successfully', 'success');
        this.assigningTicketId = null;
        this.loadTickets();
      },
      error: (err) => {
        let displayMessage = 'Assignment failed'; if (err.error?.message) {
          const match = err.error.message.match(/"message":"([^"]+)"/);
          if (match && match[1]) { displayMessage = match[1]; }
          else { displayMessage = err.error.message; }
        } this.toast.show(displayMessage, 'error');
      }
    });
  }

  autoAssign(ticket: any) {
    this.assignmentService.autoAssign(ticket.id).subscribe({
      next: () => {
        this.toast.show('Auto-assignment successful', 'success');
        this.loadTickets();
      },
      error: () => this.toast.show('No available agents found', 'error')
    });
  }

  reopenTicket(ticket: any) {

    this.ticketListService.reopenTicket(ticket.id).subscribe({

      next: () => {
        this.toast.show('Ticket reopened successfully', 'success');
        this.loadTickets();
      },
      error: (err) => {
        let displayMessage = 'Failed to reopen ticket';
        if (err.error?.message) {
          const match = err.error.message.match(/"message":"([^"]+)"/);
          if (match && match[1]) {
            displayMessage = match[1];
          } else {
            displayMessage = err.error.message;
          }
        }
        this.toast.show(displayMessage, 'error');
        console.log(err);
      }
    });
  }


}