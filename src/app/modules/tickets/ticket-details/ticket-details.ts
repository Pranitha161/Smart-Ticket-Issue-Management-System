import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TicketService } from '../../../core/services/ticket';
import { AuthService } from '../../../core/services/auth';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Activity } from '../../../core/services/activity';
import { CategoryDto } from '../../../core/services/category';
import { LookupService } from '../../../core/services/lookup-service';

@Component({
  selector: 'app-ticket-details',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ticket-details.html',
  styleUrls: ['./ticket-details.css']
})
export class TicketDetails implements OnInit {
  ticket: any;
  activities: any[] = [];
  newComment = '';
  categories: CategoryDto[] = [];

  constructor(
    private route: ActivatedRoute,
    private ticketService: TicketService,
    private authService: AuthService,
    private activityService: Activity,
    private lookup: LookupService,
    private cd: ChangeDetectorRef) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!; this.ticketService.getTicketById(id).subscribe(res => {
      this.ticket = {
        ...res,
        categoryName: this.lookup.getCategoryName(res.categoryId), 
        createdByName: this.lookup.getUserName(res.createdBy), 
        createdByEmail:this.lookup.getUserEmail(res.createdBy),
        assignedToName: this.lookup.getUserName(res.assignedTo),
        assignedToEmail:this.lookup.getUserEmail(res.assignedTo),
      }; this.cd.detectChanges();
    }); 
    this.activityService.getTicketActivity(id).subscribe(res => { 
      this.activities = Array.isArray(res) ? res : [res]; this.cd.detectChanges(); 
    });
  }

  addComment(): void {
    const actorId = this.authService.userId()!;
    this.activityService.logActivity(this.ticket.id, actorId, this.newComment).subscribe(act => {
      this.activities.push(act);
      this.newComment = '';
    });
  }
}

