import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { StudentProfileDto } from '../../../core/models/student.model';
import { ReportDto } from '../../../core/models/report.model';
import { PagedResponse } from '../../../core/models/api.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

@Component({
  selector: 'app-faculty-dashboard',
  standalone: true,
  imports: [RouterLink, MatCardModule, MatIconModule, MatButtonModule, StatusBadgeComponent, LoadingSpinnerComponent],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {
  private api = inject(ApiService);
  authService = inject(AuthService);

  menteesCount = signal(0);
  pendingReportsCount = signal(0);
  recentReports = signal<ReportDto[]>([]);
  loading = signal(true);

  userName = computed(() => this.authService.currentUser()?.fullName ?? 'Faculty');

  ngOnInit(): void {
    this.api.get<StudentProfileDto[]>('/api/faculty/me/mentees').subscribe({
      next: res => {
        const mentees = Array.isArray(res.data) ? res.data : [];
        this.menteesCount.set(mentees.length);
      }
    });

    this.api.get<PagedResponse<ReportDto>>('/api/reports', { status: 'SUBMITTED', page: 0, size: 5 }).subscribe({
      next: res => {
        this.recentReports.set(res.data.content);
        this.pendingReportsCount.set(res.data.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
