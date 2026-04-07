import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ApplicationDto } from '../../../core/models/application.model';
import { JobDto } from '../../../core/models/job.model';
import { PagedResponse } from '../../../core/models/api.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

@Component({
  selector: 'app-employer-dashboard',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatIconModule, MatButtonModule, StatusBadgeComponent, LoadingSpinnerComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  private api = inject(ApiService);
  authService = inject(AuthService);

  activeJobs = signal(0);
  totalApplications = signal(0);
  pendingInterviews = signal(0);
  recentApplications = signal<ApplicationDto[]>([]);
  loading = signal(true);

  userName = computed(() => this.authService.currentUser()?.fullName ?? 'Employer');

  ngOnInit(): void {
    this.api.get<PagedResponse<JobDto>>('/api/jobs/my', { page: 0, size: 1 }).subscribe({
      next: res => this.activeJobs.set(res.data.totalElements)
    });

    this.api.get<PagedResponse<ApplicationDto>>('/api/applications/employer', { page: 0, size: 5 }).subscribe({
      next: res => {
        this.recentApplications.set(res.data.content);
        this.totalApplications.set(res.data.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });

    this.api.get<PagedResponse<ApplicationDto>>('/api/applications/employer', {
      status: 'INTERVIEW_SCHEDULED', page: 0, size: 1
    }).subscribe({
      next: res => this.pendingInterviews.set(res.data.totalElements)
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
