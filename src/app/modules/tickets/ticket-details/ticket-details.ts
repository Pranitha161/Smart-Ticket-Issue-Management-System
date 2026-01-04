// import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
// import { ActivatedRoute, RouterLink } from '@angular/router';
// import { TicketService } from '../../../core/services/ticket';
// import { AuthService } from '../../../core/services/auth';
// import { CommonModule } from '@angular/common';
// import { FormsModule } from '@angular/forms';
// import { Activity } from '../../../core/services/activity';
// import { CategoryDto } from '../../../core/services/category';
// import { LookupService } from '../../../core/services/lookup-service';

// @Component({
//   selector: 'app-ticket-details',
//   standalone: true,
//   imports: [CommonModule, FormsModule, RouterLink],
//   templateUrl: './ticket-details.html',
//   styleUrls: ['./ticket-details.css']
// })
// export class TicketDetails implements OnInit {
//   ticket: any;
//   activities: any[] = [];
//   newComment = '';
//   categories: CategoryDto[] = [];
//   constructor(
//     private route: ActivatedRoute,
//     private ticketService: TicketService,
//     private authService: AuthService,
//     private activityService: Activity,
//     public lookup: LookupService,
//     private cd: ChangeDetectorRef) { }

//   ngOnInit(): void {
//     const id = this.route.snapshot.paramMap.get('id')!; this.ticketService.getTicketById(id).subscribe(res => {
//       this.ticket = {
//         ...res,
//         categoryName: this.lookup.getCategoryName(res.categoryId), 
//         createdByName: this.lookup.getUserName(res.createdBy), 
//         createdByEmail:this.lookup.getUserEmail(res.createdBy),
//         assignedToName: this.lookup.getUserName(res.assignedTo),
//         assignedToEmail:this.lookup.getUserEmail(res.assignedTo),
//       }; 
//       console.log(this.ticket);
//       this.cd.detectChanges();
//     }); 
//     console.log(id);
//     this.activityService.getTicketActivity(id).subscribe(res => { 
//       this.activities = Array.isArray(res) ? res : [res]; this.cd.detectChanges(); 
//       console.log(this.activities);
//     });
//   }

//   addComment(): void {
//     const actorId = this.authService.userId()!;
//     this.activityService.logActivity(this.ticket.id, actorId, this.newComment).subscribe(act => {
//       this.activities.push(act);
//       this.newComment = '';
//     });
//   }
// }



import { ChangeDetectorRef, Component, computed, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TicketService } from '../../../core/services/ticket';
import { AuthService } from '../../../core/services/auth';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Activity } from '../../../core/services/activity';
import { CategoryDto } from '../../../core/services/category';
import { LookupService } from '../../../core/services/lookup-service';
import { Toast } from '../../../core/services/toast'; // Import your service

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
    public lookup: LookupService,
    private toast: Toast, // Inject your Toast service
    private cd: ChangeDetectorRef) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.ticketService.getTicketById(id).subscribe({
      next: (res) => {
        this.ticket = {
          ...res,
          categoryName: this.lookup.getCategoryName(res.categoryId),
          createdByName: this.lookup.getUserName(res.createdBy),
          createdByEmail: this.lookup.getUserEmail(res.createdBy),
          assignedToName: this.lookup.getUserName(res.assignedTo),
          assignedToEmail: this.lookup.getUserEmail(res.assignedTo),
        };
        this.cd.detectChanges();
      },
      error: () => this.toast.show('Failed to load ticket details', 'error')
    });
    

    this.activityService.getTicketActivity(id).subscribe(res => {
      this.activities = Array.isArray(res) ? res : [res];
      this.cd.detectChanges();
    });
  }

  
  addComment(): void {
    if (!this.newComment.trim()) {
      this.toast.show('Please enter a message', 'error');
      return;
    }

    const actorId = this.authService.userId()!;
    this.activityService.logActivity(this.ticket.id, actorId, this.newComment).subscribe({
      next: (act) => {
        this.activities.push(act);
        this.newComment = '';
        this.toast.show('Update posted successfully', 'success');
        this.cd.detectChanges();
      },
      error: () => this.toast.show('Error posting update', 'error')
    });
  }

  canResolve = computed(() => {
    const role = this.authService.roles()[0];
    const userId = this.authService.userId();
    
    // Allow if user is ADMIN or the specific AGENT assigned to this ticket
    return role === 'ADMIN' || (role === 'AGENT' && this.ticket?.assignedTo === userId);
  });

  // Modify resolveTicket to handle the state check
  resolveTicket(): void {
    if (this.ticket.status === 'RESOLVED') return;

    this.ticketService.resolveTicket(this.ticket.id).subscribe({
      next: () => {
        this.ticket.status = 'RESOLVED';
        this.toast.show('Ticket marked as Resolved', 'success');
        
        // Optional: Refresh activity to show the resolution log
        this.refreshActivity(); 
        this.cd.detectChanges();
      },
      error: () => this.toast.show('Failed to resolve ticket', 'error')
    });
  }

  private refreshActivity() {
    this.activityService.getTicketActivity(this.ticket.id).subscribe(res => {
      this.activities = Array.isArray(res) ? res : [res];
      this.cd.detectChanges();
    });
  }
}
 
