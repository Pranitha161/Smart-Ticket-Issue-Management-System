import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  imports: [RouterModule,CommonModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar implements OnInit{
  isLoggedIn = false;
  constructor(private authService: AuthService) {}
  ngOnInit(): void { 
     this.isLoggedIn  = this.authService.isLoggedIn();
  }

}
