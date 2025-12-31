
// import { Component, OnInit } from '@angular/core';
// import { TicketService } from '../../../core/services/ticket';
// import { CommonModule } from '@angular/common';
// import { RouterLink } from '@angular/router';
// import { FormsModule } from '@angular/forms';

// @Component({
//   selector: 'app-ticket-list',
//   standalone: true,
//   imports:[CommonModule,RouterLink,FormsModule],
//   templateUrl: './ticket-list.html',
//   styleUrls: ['./ticket-list.css']
// })
// export class TicketList implements OnInit {
//   tickets: any[] = [];            // all tickets
//   filteredTickets: any[] = [];    // filtered by search
//   pagedTickets: any[] = [];       // current page slice
//   searchTerm = '';
//   currentPage = 0;
//   pageSize = 5;
//   totalPages = 0;

//   constructor(private ticketService: TicketService) {}

//   ngOnInit(): void {
//     this.ticketService.getTickets().subscribe({
//       next: (res) => {
//         this.tickets = res;
//         this.filteredTickets = res;
//         this.totalPages = Math.ceil(this.filteredTickets.length / this.pageSize);
//         this.setPage(0);
//       },
//       error: () => console.error('Failed to load tickets')
//     });
//   }

//   private safeIncludes(field: string | null | undefined, term: string): boolean {
//   return field ? field.toLowerCase().includes(term) : false;
// }
// searchTermPriority = '';
// selectedPriority = '';

// applyFilters(): void {
//   const term = this.searchTerm.toLowerCase().trim();

//   this.filteredTickets = this.tickets.filter(ticket => {
//     const matchesSearch =
//       (ticket.title && ticket.title.toLowerCase().includes(term)) ||
//       (ticket.displayId && ticket.displayId.toLowerCase().includes(term)) ||
//       (ticket.categoryId && ticket.categoryId.toLowerCase().includes(term));

//     const matchesPriority =
//       !this.selectedPriority || ticket.priority === this.selectedPriority;

//     return matchesSearch && matchesPriority;
//   });

//   this.totalPages = Math.ceil(this.filteredTickets.length / this.pageSize);
//   this.setPage(0);
// }

// applySearchPriority(): void {
//   this.applyFilters(); // reuse same logic
// }


// applySearch(): void {
//   const term = this.searchTerm.toLowerCase().trim();

//   this.filteredTickets = this.tickets.filter(ticket =>
//     this.safeIncludes(ticket.title, term) ||
//     this.safeIncludes(ticket.displayId, term) ||
//     this.safeIncludes(ticket.categoryId, term)
//   );

//   this.totalPages = Math.ceil(this.filteredTickets.length / this.pageSize);
//   this.setPage(0);
// }


//   setPage(page: number): void {
//     if (page >= 0 && page < this.totalPages) {
//       this.currentPage = page;
//       const start = page * this.pageSize;
//       const end = start + this.pageSize;
//       this.pagedTickets = this.filteredTickets.slice(start, end);
//     }
//   }
// }
import { Component, OnInit } from '@angular/core';
import { TicketService } from '../../../core/services/ticket';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth';

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

  constructor(private ticketService: TicketService, private authService: AuthService) { }

  ngOnInit(): void {
    if (this.authService.hasRole('USER')) {
      const userId = this.authService.getUserId();
      console.log(userId)
      this.ticketService.getTicketsByUserId(userId).subscribe(
        {
          next: (res) => {
            this.tickets = res;
            this.filteredTickets = res;
            this.totalPages = Math.ceil(this.filteredTickets.length / this.pageSize);
            this.setPage(0);
          }, error: () =>
            console.error('Failed to load tickets for user')
        });
    }
    else { this.ticketService.getTickets().subscribe({ next: (res) => { this.tickets = res; this.filteredTickets = res; this.totalPages = Math.ceil(this.filteredTickets.length / this.pageSize); this.setPage(0); }, error: () => console.error('Failed to load tickets') }); }
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
        this.safeIncludes(ticket.categoryId, term);

      const matchesPriority =
        !this.selectedPriority || ticket.priority === this.selectedPriority;
      const matcheStatus =
        !this.selectedStatus || ticket.status === this.selectedStatus;

      return matchesSearch && matchesPriority && matcheStatus;
    });

    this.totalPages = Math.ceil(this.filteredTickets.length / this.pageSize);
    this.setPage(0);
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
}
