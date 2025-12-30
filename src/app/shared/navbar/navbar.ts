import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
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
  constructor(private authService: AuthService) { }
  ngOnInit() {
    if (this.authService.isLoggedIn()) {
      this.username = this.authService.getUsername();
    }
  }
  get firstLetter(): string { return this.username ? this.username.charAt(0).toUpperCase() : ''; }

}
