import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminUser } from '../../../core/services/admin-user';
import { Toast } from '../../../core/services/toast';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrls: ['./forgot-password.css']
})
export class ForgotPassword {
  email = '';
  private adminUser = inject(AdminUser);
  private toast = inject(Toast);
  constructor(private router: Router) { }
  submit(): void {
    if (!this.email) {
      this.toast.show('Please enter your email address.', 'error');
      return;
    }
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(this.email)) {
      this.toast.show('Please enter a valid email address.', 'error');
      return;
    }

    this.adminUser.requestPasswordReset(this.email).subscribe({
      next: (response: any) => {
        const successMsg = response?.message || 'Password reset link sent!';
        this.toast.show(successMsg, 'success');
        setTimeout(() => this.router.navigate(['/auth/profile']), 2000);

      },
      error: (err) => {
        const backendMsg = err.error?.message || err.statusText || 'An unexpected error occurred';
        this.toast.show(backendMsg, 'error');
      }
    });
  }
}
