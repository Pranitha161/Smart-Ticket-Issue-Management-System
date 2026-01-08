import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { CommonModule } from '@angular/common';
import { Toast } from '../../../core/services/toast'; //

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './signup.html',
  styleUrls: ['./signup.css']
})
export class Signup implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(Toast);

  signupForm!: FormGroup;

  ngOnInit(): void {
    this.signupForm = this.fb.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      role: ['USER', Validators.required],
      agentLevel: ['']
    },{ validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
  const password = form.get('password')?.value;
  const confirmPassword = form.get('confirmPassword')?.value;
  if (!password || !confirmPassword) { return null; }
  return password === confirmPassword ? null : { passwordMismatch: true };
}


  onSubmit(): void {
    if (this.signupForm.invalid) {
      this.toast.show('Please fill in all required fields correctly', 'error');
      return;
    }

    const formValue = { ...this.signupForm.value };
    delete formValue.confirmPassword;
    formValue.roles = [formValue.role];
    delete formValue.role;
    if (formValue.roles[0] !== 'AGENT') {
      delete formValue.agentLevel;
    }

    this.authService.signup(formValue).subscribe({
      next: (res) => {
        if (res.success) {
          this.toast.show('Account created successfully!', 'success');
          this.router.navigate(['/auth/login']);
        } else {
          this.toast.show(res.message || 'Signup failed', 'error');
        }
      },
      error: (err) => {
        const errorMsg = err.error?.message || err.message || 'Signup failed. Please try again.';
        this.toast.show(errorMsg, 'error');
      }
    });
  }
}