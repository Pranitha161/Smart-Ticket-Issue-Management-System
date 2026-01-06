// notifications.component.ts
import { Component, OnInit } from '@angular/core';
import { Notification } from '../../../shared/models/notifications.model';
import { Notifications } from '../../../core/services/notifications';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-notifications',

  imports: [CommonModule, FormsModule,RouterLink],
  templateUrl: './notification-panel.html',
  styleUrls: ['./notification-panel.css']
})
export class NotificationPanel implements OnInit {
  

  formNote: Notification = {
    sender: '',
    recipient: '',
    subject: '',
    body: ''
  };

  constructor(private service: Notifications, private authService: AuthService) { }
  get history() {
    return this.service.historySignal;
  }
  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    const currentUser = this.authService.email();
    if (currentUser) {
      this.formNote.sender = currentUser;
      this.service.refreshHistory(currentUser);
    }
  }

  onSubmit(): void {
    this.service.sendNotification(this.formNote).subscribe({
      next: () => {
        this.refresh(); 
        this.resetForm();
      }
    });
  }

  private resetForm() {
    this.formNote = { 
      sender: this.authService.email() || '', 
      recipient: '', 
      subject: '', 
      body: '' 
    };
  }
}