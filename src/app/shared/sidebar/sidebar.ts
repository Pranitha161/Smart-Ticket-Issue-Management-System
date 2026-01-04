import { Component, computed, inject } from '@angular/core'; // Added computed and inject
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {

  private authService = inject(AuthService);

  isLoggedIn = computed(() => {this.authService.loggedIn();});
  userRole = computed(() => {
    // 1. First, call the signal as a function to get the string[]
    const currentRoles = this.authService.roles(); 

    // 2. Now you can check the length and access index [0] on the array
    return currentRoles && currentRoles.length > 0 ? currentRoles[0] : null;
  });

}