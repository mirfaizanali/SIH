import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { AuthService } from '../../../core/services/auth.service';
import { Role } from '../../../core/models/user.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatSelectModule],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  authService = inject(AuthService);
  router = inject(Router);
  fb = inject(FormBuilder);

  form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role:     ['STUDENT' as Role, Validators.required]
  });

  loading = signal(false);
  error = signal('');

  roles = [
    { value: 'STUDENT',           label: 'Student' },
    { value: 'FACULTY_MENTOR',    label: 'Faculty Mentor' },
    { value: 'EMPLOYER',          label: 'Employer / Recruiter' },
    { value: 'PLACEMENT_OFFICER', label: 'Placement Cell Officer' }
  ];

  submit() {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.error.set('');
    const { email, password, fullName, role } = this.form.getRawValue();
    this.authService.register(email, password, fullName, role).subscribe({
      next: () => {
        const map: Record<string, string> = {
          STUDENT:           '/student/dashboard',
          FACULTY_MENTOR:    '/faculty/dashboard',
          PLACEMENT_OFFICER: '/officer/dashboard',
          EMPLOYER:          '/employer/dashboard'
        };
        this.router.navigate([map[role] ?? '/auth/login']);
      },
      error: (err) => {
        this.error.set(err.error?.message ?? 'Registration failed. Please try again.');
        this.loading.set(false);
      }
    });
  }
}
