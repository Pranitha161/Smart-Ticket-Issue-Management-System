import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { AdminUser } from '../../../core/services/admin-user';
import { Toast } from '../../../core/services/toast';
import { User } from '../../../shared/models/authuser.model';


@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css']
})
export class Profile implements OnInit {
  private toast = inject(Toast);

  user: User | null = null;
  userId: string = '';
  activeTab: 'edit' | 'password' | null = null;

  oldPassword = '';
  newPassword = '';

  constructor(
    private authService: AuthService,
    private adminUser: AdminUser,
    private cd: ChangeDetectorRef,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.userId = this.authService.userId()!;
    this.adminUser.getUserById(this.userId).subscribe(data => {
      this.user = data;
      this.cd.detectChanges();
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  updateUser(): void {
    if (this.user) {
      this.adminUser.updateUser(this.userId, this.user).subscribe({
        next: (updated) => {
          this.user = updated;
          this.toast.show('Profile updated successfully!', 'success');
          this.goBack();
        }
      });
    }
  }

  changePassword(): void {
    if (!this.oldPassword || !this.newPassword) {
      this.toast.show('Please enter both old and new password.', 'error');
      return;
    }

    this.adminUser.changePassword(this.user!.username, this.oldPassword, this.newPassword)
      .subscribe({
        next: () => {
          this.toast.show('Password changed successfully!', 'success');
          this.goBack();
        }
      });
  }
}