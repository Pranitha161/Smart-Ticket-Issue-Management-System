import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth';
import { CommonModule } from '@angular/common';

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

  username = computed(() => this.authService.username());
  userRoles = computed(() => this.authService.roles());
  hasLoggedIn = computed(() => this.authService.loggedIn());
  
  currentUrl = '';

  constructor() {
    this.router.events.subscribe(() => {
      this.currentUrl = this.router.url;
    });
  }

  get firstLetter(): string {
    const name = this.username();
    return name ? name.charAt(0).toUpperCase() : '';
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