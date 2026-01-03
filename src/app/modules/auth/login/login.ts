import { Component, OnInit, inject } from '@angular/core'; // Added inject
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { Toast } from '../../../core/services/toast';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login implements OnInit {
  loginForm!: FormGroup;
  private toast = inject(Toast);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    const token = this.authService.getToken();
    if (token) {
      this.router.navigate(['/dashboard']);
    }
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.authService.login(this.loginForm.value).subscribe({
        next: (res: any) => {
          if (res.message) {
            this.authService.setToken(res.message);
            this.toast.show('Login successful! Redirecting...', 'success');
            this.router.navigate(['/tickets']);
          } else {
            this.toast.show('Login failed. No token received.', 'error');
          }
        }
      });
    } else {
      this.toast.show('Please fill in all required fields.', 'error');
    }
  }
}