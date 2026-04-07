import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface SystemStats {
  totalUsers: number;
  activeUsers: number;
  totalStudents: number;
  totalEmployers: number;
  totalFaculty: number;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatIconModule, MatButtonModule, LoadingSpinnerComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class AdminDashboard implements OnInit {
  private api = inject(ApiService);
  authService = inject(AuthService);

  stats = signal<SystemStats | null>(null);
  loading = signal(true);

  ngOnInit(): void {
    this.api.get<SystemStats>('/api/admin/stats').subscribe({
      next: res => {
        this.stats.set(res.data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }
}
