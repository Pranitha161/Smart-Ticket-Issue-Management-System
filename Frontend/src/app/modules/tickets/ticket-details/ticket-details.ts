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
  role:string='';
  categories: CategoryDto[] = [];
  pendingAction: 'START' | 'RESOLVE' | 'REOPEN' | null = null;
  constructor(
    private route: ActivatedRoute,
    private ticketService: TicketService,
    private authService: AuthService,
    public activityService: Activity,
    public lookup: LookupService,
    private toast: Toast,
    private cd: ChangeDetectorRef) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    const roles = this.authService.roles();
    this.role = roles && roles.length > 0 ? roles[0] : '';
   
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
      const acts = Array.isArray(res) ? res : [res];
      this.activities = acts.map(act => ({ ...act, actorName: this.lookup.getUserName(act.actorId), actorEmail: this.lookup.getUserEmail(act.actorId) }));
      this.cd.detectChanges();
    });

  }
  processAction() {
    if (!this.pendingAction) return;

    if (this.pendingAction === 'START') {
      this.startWork();
    } else if (this.pendingAction === 'RESOLVE') {
      this.resolveTicket();
    } 
    else if (this.pendingAction === 'REOPEN') {
      this.reopenTicket();
    }

    this.pendingAction = null; // Close modal after action
  }
  openConfirm(action: 'START' | 'RESOLVE' | 'REOPEN') {
  this.pendingAction = action;
}
  reopenTicket(): void {
    // Note: Assuming you have a ticketListService or similar injected, 
    // or you can add a method to ticketService
    this.ticketService.reopenTicket(this.ticket.id).subscribe({
      next: () => {
        this.toast.show('Ticket reopened successfully', 'success');
        this.refreshTicketData(); // Helper to reload ticket
        this.refreshActivity();
      },
      error: () => this.toast.show('Failed to reopen ticket', 'error')
    });
  }
  private refreshTicketData() {
    this.ticketService.getTicketById(this.ticket.id).subscribe(res => {
      this.ticket = {
        ...res,
        categoryName: this.lookup.getCategoryName(res.categoryId),
        createdByName: this.lookup.getUserName(res.createdBy),
        assignedToName: this.lookup.getUserName(res.assignedTo),
      };
      this.cd.detectChanges();
    });
  }

  startWork(): void {
  if (!this.ticket || this.ticket.status !== 'ASSIGNED') return;

  const agentId = this.authService.userId()!;

  this.ticketService.startWorkOnTicket(this.ticket.id, agentId).subscribe({
    next: (updatedTicket) => {
      this.ticket = {
        ...updatedTicket,
        categoryName: this.lookup.getCategoryName(updatedTicket.categoryId),
        createdByName: this.lookup.getUserName(updatedTicket.createdBy),
        createdByEmail: this.lookup.getUserEmail(updatedTicket.createdBy),
        assignedToName: this.lookup.getUserName(updatedTicket.assignedTo),
        assignedToEmail: this.lookup.getUserEmail(updatedTicket.assignedTo),
      };
      this.toast.show('Work started on ticket', 'success');
      this.refreshActivity(); 
      this.cd.detectChanges();
    },
    error: () => this.toast.show('Failed to start work', 'error')
  });
}

canStartWork(): boolean {
  const role = this.authService.roles()[0];
  const userId = this.authService.userId();

  // Only allow if agent, ticket is ASSIGNED, and assigned to this agent
  return role === 'AGENT' &&
         this.ticket?.status === 'ASSIGNED' &&
         this.ticket?.assignedTo === userId;
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

  canResolve(): boolean {
    const role = this.authService.roles()[0]; const userId = this.authService.userId(); if (!this.ticket) return false;
    return role === 'ADMIN' || (role === 'AGENT' && this.ticket.assignedTo === userId);
  }
  resolveTicket(): void {
    if (this.ticket.status === 'RESOLVED') return;

    this.ticketService.resolveTicket(this.ticket.id).subscribe({
      next: () => {
        this.ticket.status = 'RESOLVED';
        this.toast.show('Ticket marked as Resolved', 'success');
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

