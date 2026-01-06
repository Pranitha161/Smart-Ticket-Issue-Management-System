import { Component, computed, effect, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth';
import { CommonModule } from '@angular/common';
import { NotificationPanel } from '../../modules/notifications/notification-panel/notification-panel';
import { Notifications } from '../../core/services/notifications';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css'],
})
export class Navbar {
  private authService = inject(AuthService);
  private router = inject(Router);
  private notificationService = inject(Notifications);

  notificationHistory = this.notificationService.historySignal;
  username = computed(() => this.authService.username());
  userRoles = computed(() => this.authService.roles());
  hasLoggedIn = computed(() => this.authService.loggedIn());

  currentUrl = '';
  public isNavbarCleared = signal(false);

  constructor() {
    this.router.events.subscribe(() => {
      this.currentUrl = this.router.url;
    });
    effect(() => {
      this.notificationService.historySignal();
      this.isNavbarCleared.set(false);
    });
  }

  get firstLetter(): string {
    const name = this.username();
    return name ? name.charAt(0).toUpperCase() : '';
  }

  displayNotifications = computed(() => {
    const fullHistory = this.notificationService.historySignal();
    return this.isNavbarCleared() ? [] : fullHistory.slice(0, 5);
  });

  // 3. Simple method to hide them
  clearNavbarView(): void {
    this.isNavbarCleared.set(true);
  }

  hasUserRole(): boolean {
    return this.userRoles().includes('USER');
  }

  isOnCreateTicket(): boolean {
    return this.currentUrl.includes('/tickets/create');
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}