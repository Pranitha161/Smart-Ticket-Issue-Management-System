import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketService } from '../../../core/services/ticket';
import { AuthService } from '../../../core/services/auth';
import { Ticket } from '../../../shared/models/ticket.model';
import { RouterLink } from '@angular/router';

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

  constructor(private ticketService: TicketService, private authService: AuthService,private cd:ChangeDetectorRef) { }

  ngOnInit(): void {
    const userId = this.authService.getUserId();
    this.role = this.authService.getUserRoles()[0];

    if (this.role === 'ADMIN' || this.role === 'MANAGER') {
      this.ticketService.getRecentTickets().subscribe(data => {this.tickets = data;
        this.cd.detectChanges();
      });
    }
    else if (this.role === 'AGENT') {
      const agentId = this.authService.getUserId();
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
