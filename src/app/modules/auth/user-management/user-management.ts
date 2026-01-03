import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminUser, User, UserStatsDto } from '../../../core/services/admin-user';
import { AuthService } from '../../../core/services/auth';
import { CategoryDto } from '../../../core/services/category';
import { LookupService } from '../../../core/services/lookup-service';
import { Toast } from '../../../core/services/toast';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-management.html',
  styleUrls: ['./user-management.css']
})
export class UserManagement implements OnInit {
  private adminUserService = inject(AdminUser);
  private authService = inject(AuthService);
  private lookup = inject(LookupService);
  private toast = inject(Toast);
  private cd = inject(ChangeDetectorRef);

  users: User[] = [];
  categories: CategoryDto[] = [];
  userStats: UserStatsDto | null = null;
  modalMode: 'create' | 'edit' | null = null;
  skillsInput: string = '';

  formUser: any = this.getEmptyUser();

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.users = this.lookup.getUserList();
    this.categories = this.lookup.getCategoryList();
    this.adminUserService.getUserStats().subscribe(stats => {
      this.userStats = stats;
      this.cd.detectChanges();
    });
  }

  openCreateForm() {
    this.modalMode = 'create';
    this.formUser = this.getEmptyUser();
    this.skillsInput = '';
  }

  editUser(user: User) {
    this.modalMode = 'edit';
    this.formUser = { ...user };
    if (user.roles?.[0] === 'AGENT' && user['agentProfile']) {
      this.skillsInput = user['agentProfile'].skills?.join(', ') || '';
    }
  }

  onRoleChange(role: string) {
    this.formUser.roles = [role];
    this.formUser.agentProfile = role === 'AGENT' ? { agentLevel: '', categoryId: '', skills: [], currentAssignments: 0 } : null;
  }

  saveUser() {
    const payload = { ...this.formUser };
    if (this.formUser.roles[0] === 'AGENT') {
      payload.agentProfile.skills = this.skillsInput.split(',').map(s => s.trim()).filter(s => s.length > 0);
    }

    const request = this.modalMode === 'create'
      ? this.authService.create(payload)
      : this.adminUserService.updateUser(this.formUser.id, payload);

    request.subscribe({
      next: (res) => {
        this.toast.show(`User ${this.modalMode}d successfully`, 'success');
        this.lookup.refreshUsers();
        this.loadData();
        this.cancel();
      }
    });
  }

  userToDelete: User | null = null;

  deleteUser(user: User): void {
    this.userToDelete = user;
  }

  confirmDelete(): void {
    if (this.userToDelete) {
      this.adminUserService.deleteUser(this.userToDelete.id).subscribe({
        next: () => {
          this.users = this.users.filter(u => u.id !== this.userToDelete?.id);
          this.toast.show('User removed successfully', 'success');
          this.lookup.refreshUsers();
          this.userToDelete = null;
          this.cd.detectChanges();
        },
        error: () => {
          this.toast.show('Error deleting user', 'error');
          this.userToDelete = null;
        }
      });
    }
  }

  cancel() { this.modalMode = null; }

  private getEmptyUser() {
    return { username: '', email: '', password: '', roles: ['USER'], agentProfile: null };
  }
}