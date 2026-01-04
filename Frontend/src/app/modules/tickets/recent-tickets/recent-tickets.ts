import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketService } from '../../../core/services/ticket';
import { AuthService } from '../../../core/services/auth';
import { Ticket } from '../../../shared/models/ticket.model';
import { RouterLink } from '@angular/router';
import { LookupService } from '../../../core/services/lookup-service';

@Component({
  selector: 'app-recent-tickets',
  standalone: true,
  imports: [CommonModule,RouterLink],
  templateUrl: './recent-tickets.html',
  styleUrls: ['./recent-tickets.css']
})
export class RecentTickets implements OnInit {
  tickets: Ticket[] = [];
  role: string = '';
  isNearBreach(dueAt: string): boolean {
  const due = new Date(dueAt).getTime();
  return due - Date.now() < 60 * 60 * 1000; 
}
 public lookup = inject(LookupService);

  constructor(private ticketService: TicketService, private authService: AuthService,private cd:ChangeDetectorRef) { }

  ngOnInit(): void {
    const userId = this.authService.userId()!;
    this.role = this.authService.roles()[0];

    if (this.role === 'ADMIN' || this.role === 'MANAGER') {
      this.ticketService.getRecentTickets().subscribe(data => {this.tickets = data;
        this.cd.detectChanges();
      });
    }
    else if (this.role === 'AGENT') {
      const agentId = this.authService.userId()!;
      this.ticketService.getRecentTicketsByAgent(agentId).subscribe(data => {this.tickets = data;
        this.cd.detectChanges();
      });
      
      
    }
    else {
      this.ticketService.getRecentTicketsByUser(userId).subscribe(data => {this.tickets = data
        this.cd.detectChanges();
      });
    }
  }
}
