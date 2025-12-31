import { Component } from '@angular/core';
import {Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  username: string | null = null;
  userRole:string[]=[];
  currentUrl: string = '';
  constructor(private authService: AuthService,private router:Router) { }
  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.username = this.authService.getUsername();
      this.userRole=this.authService.getUserRoles();
    }
    this.router.events.subscribe(() => { this.currentUrl = this.router.url; });
  }

  hasUserRole(): boolean { return this.userRole.includes('USER'); }
  get firstLetter(): string { return this.username ? this.username.charAt(0).toUpperCase() : ''; }
  isOnCreateTicket(): boolean { return this.currentUrl.includes('/tickets/create'); }

}
