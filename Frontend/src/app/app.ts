import { Component, computed, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from './shared/navbar/navbar';
import { Sidebar } from './shared/sidebar/sidebar';
import { ToastComponent } from './shared/components/toast/toast';
import { AuthService } from './core/services/auth';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet,
    CommonModule,
    ToastComponent,
    Navbar,Sidebar],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private auth = inject(AuthService);
  isLoggedIn = computed(() => this.auth.loggedIn());
  protected readonly title = signal('smart-ticket-frontend');
}
