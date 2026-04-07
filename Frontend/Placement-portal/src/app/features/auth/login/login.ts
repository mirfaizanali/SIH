import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  authService = inject(AuthService);
  router = inject(Router);
  form = inject(FormBuilder).nonNullable.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });
  loading = signal(false);
  error = signal('');

  submit() {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.error.set('');
    const { email, password } = this.form.getRawValue();
    this.authService.login(email, password).subscribe({
      next: (res) => {
        const role = res.data.role;
        const map: Record<string, string> = {
          STUDENT:           '/student/dashboard',
          FACULTY_MENTOR:    '/faculty/dashboard',
          PLACEMENT_OFFICER: '/officer/dashboard',
          EMPLOYER:          '/employer/dashboard',
          ADMIN:             '/admin/dashboard'
        };
        this.router.navigate([map[role] ?? '/auth/login']);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Invalid email or password');
        this.loading.set(false);
      }
    });
  }
}
