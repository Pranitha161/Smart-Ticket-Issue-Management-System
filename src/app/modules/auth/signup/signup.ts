import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './signup.html',
  styleUrls: ['./signup.css']
})
export class Signup implements OnInit {
  signupForm!: FormGroup;
  message: string = '';

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) { }

  ngOnInit(): void {
    this.signupForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      role: ['USER', Validators.required],
      agentLevel: ['']
    });
  }

  onSubmit(): void {
    if (this.signupForm.valid) {
      const formValue = { ...this.signupForm.value };

      formValue.roles = [formValue.role]; delete formValue.role;
      if (formValue.roles[0] !== 'AGENT') { delete formValue.agentLevel; }
      console.log(formValue);
      this.authService.signup(formValue).subscribe({
        next: (res) => {
          this.message = res.message;
          if (res.success) {
            this.router.navigate(['/login']);
          }
        },
        error: (err) => {
          this.message = err.error?.message || 'Signup failed. Try again.';
        }
      });
    }
  }
}
