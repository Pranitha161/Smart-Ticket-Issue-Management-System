import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminUser } from '../../../core/services/admin-user';
import { Toast } from '../../../core/services/toast';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reset-password.html',
  styleUrls: ['./reset-password.css']
})
export class ResetPassword implements OnInit {
  token = '';
  newPassword = '';
  message = '';
  messageType: 'success' | 'error' | '' = '';
  private toast = inject(Toast);
  constructor(
    private route: ActivatedRoute,
    private adminUser: AdminUser,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') || '';
  }

  submit(): void {
    if (!this.newPassword) {
      this.toast.show('Please enter a new password.', 'error');
      return;
    }
    if (this.newPassword.length < 6) {
      this.toast.show('Password must be at least 6 characters', 'error');
      return;
    }

    this.adminUser.resetPassword(this.token, this.newPassword).subscribe({
      next: () => {
        this.toast.show('Password updated successfully!', 'success');
        setTimeout(() => this.router.navigate(['/auth/login']), 2000);
      }

    });
  }
}
