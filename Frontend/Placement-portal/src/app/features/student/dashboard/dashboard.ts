import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ApplicationDto } from '../../../core/models/application.model';
import { PagedResponse } from '../../../core/models/api.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

@Component({
  selector: 'app-student-dashboard',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatIconModule, MatButtonModule, StatusBadgeComponent, LoadingSpinnerComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  private api = inject(ApiService);
  authService = inject(AuthService);

  stats = signal<{ applications: number; interviews: number; activeJobs: number }>({
    applications: 0,
    interviews: 0,
    activeJobs: 0
  });
  recentApplications = signal<ApplicationDto[]>([]);
  loading = signal(true);

  userName = computed(() => this.authService.currentUser()?.fullName ?? 'Student');

  ngOnInit(): void {
    this.api.get<PagedResponse<ApplicationDto>>('/api/applications/my', { page: 0, size: 5 })
      .subscribe({
        next: res => {
          this.recentApplications.set(res.data.content);
          this.stats.update(s => ({ ...s, applications: res.data.totalElements }));
          this.loading.set(false);
        },
        error: () => this.loading.set(false)
      });

    this.api.get<PagedResponse<ApplicationDto>>('/api/applications/my', { status: 'INTERVIEW_SCHEDULED', page: 0, size: 1 })
      .subscribe({
        next: res => this.stats.update(s => ({ ...s, interviews: res.data.totalElements }))
      });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
