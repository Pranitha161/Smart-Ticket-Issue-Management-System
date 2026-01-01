import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TicketService } from '../../../core/services/ticket';
import { AuthService } from '../../../core/services/auth';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-ticket-details',
  standalone: true,
  imports: [CommonModule, FormsModule,RouterLink],
  templateUrl: './ticket-details.html',
  styleUrls: ['./ticket-details.css']
})
export class TicketDetails implements OnInit {
  ticket: any;
  activities: any[] = [];
  newComment = '';

  constructor(private route: ActivatedRoute, private ticketService: TicketService, private authService: AuthService, private cd: ChangeDetectorRef) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.ticketService.getTicketById(id).subscribe(res =>{
      this.ticket = res;
    }
    );
    this.ticketService.getTicketActivity(id).subscribe(res => {
      this.activities = Array.isArray(res) ? res : [res];
    });
    this.cd.detectChanges();
  }

  addComment(): void {
    const actorId = this.authService.getUserId();
    this.ticketService.addTicketComment(this.ticket.id, actorId, this.newComment).subscribe(act => {
      this.activities.push(act);
      this.newComment = '';
    });
  }
}

