import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface DashboardAnalytics {
  totalStudents: number;
  placedStudents: number;
  placementRate: number;
  averagePackage: number;
  activeJobs: number;
  scheduledDrives: number;
}

@Component({
  selector: 'app-officer-dashboard',
  standalone: true,
  imports: [RouterLink, DecimalPipe, MatCardModule, MatIconModule, MatButtonModule, LoadingSpinnerComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  private api = inject(ApiService);
  authService = inject(AuthService);

  analytics = signal<DashboardAnalytics | null>(null);
  loading = signal(true);
  errorMsg = signal('');

  userName = computed(() => this.authService.currentUser()?.fullName ?? 'Officer');

  ngOnInit(): void {
    this.api.get<DashboardAnalytics>('/api/analytics/dashboard').subscribe({
      next: res => {
        this.analytics.set(res.data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Failed to load analytics.');
        this.loading.set(false);
      }
    });
  }

  formatPackage(value: number): string {
    if (!value) return '—';
    return `₹${(value / 100000).toFixed(1)} LPA`;
  }
}
