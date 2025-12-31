import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminUser, CreateUserRequest, User, UserStatsDto } from '../../../core/services/admin-user';


@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-panel.html',
  styleUrls: ['./admin-panel.css']
})
export class AdminPanel implements OnInit {
  users: User[] = [];
  userStats: UserStatsDto | null = null;

  modalMode: 'create' | 'edit' | null = null;
  formUser: any = { username: '', email: '', password: '', roles: ['USER'] };

  constructor(private adminUserService: AdminUser, private cd: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadUsers();
    this.adminUserService.getUserStats().subscribe(stats => {
      this.userStats = stats;
      this.cd.detectChanges();
    });
  }

  loadUsers(): void {
    this.adminUserService.getAllUsers().subscribe(res => {
      this.users = res;
      this.cd.detectChanges();
    });
  }

  openCreateForm(): void {
    this.modalMode = 'create';
    this.formUser = { username: '', email: '', password: '', roles: ['USER'] };
  }

  editUser(user: User): void {
    this.modalMode = 'edit';
    this.formUser = { ...user }; // clone user into form
  }

  saveUser(): void {
    if (this.modalMode === 'create') {
      this.adminUserService.createUser(this.formUser).subscribe(created => {
        this.users.push(created);
        this.cancel();
      });
    } else if (this.modalMode === 'edit') {
      this.adminUserService.updateUser(this.formUser.id, this.formUser).subscribe(updated => {
        const index = this.users.findIndex(u => u.id === updated.id);
        if (index !== -1) this.users[index] = updated;
        this.cancel();
        console.log(updated);
      });
    }
  }

  deleteUser(id: string): void {
    this.adminUserService.deleteUser(id).subscribe(() => {
      this.users = this.users.filter(u => u.id !== id);
    });
  }

  cancel(): void {
    this.modalMode = null;
    this.formUser = { username: '', email: '', password: '', roles: ['USER'] };
  }
}
