import { Component, OnInit, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface DashboardAnalytics {
  totalStudents: number;
  placedStudents: number;
  placementRate: number;
  averagePackage: number;
  highestPackage: number;
  lowestPackage: number;
  activeJobs: number;
  totalApplications: number;
  scheduledDrives: number;
  departmentWisePlacement?: { department: string; placed: number; total: number; rate: number }[];
}

@Component({
  selector: 'app-officer-analytics',
  standalone: true,
  imports: [MatCardModule, MatIconModule, LoadingSpinnerComponent],
  templateUrl: './analytics.html',
  styleUrl: './analytics.css'
})
export class Analytics implements OnInit {
  private api = inject(ApiService);

  analytics = signal<DashboardAnalytics | null>(null);
  loading = signal(true);
  errorMsg = signal('');

  ngOnInit(): void {
    this.api.get<DashboardAnalytics>('/api/analytics/dashboard').subscribe({
      next: res => {
        this.analytics.set(res.data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Failed to load analytics data.');
        this.loading.set(false);
      }
    });
  }

  formatPackage(value: number): string {
    if (!value) return '—';
    return `₹${(value / 100000).toFixed(2)} LPA`;
  }

  formatRate(value: number): string {
    if (!value && value !== 0) return '—';
    return `${value.toFixed(1)}%`;
  }
}
