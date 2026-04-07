import { Component, OnInit, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { ApiService } from '../../../core/services/api.service';
import { ApplicationDto } from '../../../core/models/application.model';
import { JobDto } from '../../../core/models/job.model';
import { PagedResponse } from '../../../core/models/api.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

const APPLICATION_STATUSES = ['SUBMITTED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW_SCHEDULED', 'OFFERED', 'ACCEPTED', 'REJECTED', 'WITHDRAWN'];

@Component({
  selector: 'app-employer-applicants',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatIconModule, MatSelectModule, MatFormFieldModule, StatusBadgeComponent, LoadingSpinnerComponent],
  templateUrl: './applicants.html',
  styleUrl: './applicants.css'
})
export class Applicants implements OnInit {
  private api = inject(ApiService);

  jobs = signal<JobDto[]>([]);
  selectedJob = signal<JobDto | null>(null);
  applications = signal<ApplicationDto[]>([]);
  loadingJobs = signal(true);
  loadingApps = signal(false);
  updatingId = signal<string | null>(null);
  errorMsg = signal('');

  statusOptions = APPLICATION_STATUSES;

  ngOnInit(): void {
    this.api.get<PagedResponse<JobDto>>('/api/jobs/my', { page: 0, size: 50 }).subscribe({
      next: res => {
        this.jobs.set(res.data.content);
        this.loadingJobs.set(false);
      },
      error: () => this.loadingJobs.set(false)
    });
  }

  selectJob(job: JobDto): void {
    this.selectedJob.set(job);
    this.loadingApps.set(true);
    this.api.get<PagedResponse<ApplicationDto>>(`/api/applications/job/${job.id}`, { page: 0, size: 50 }).subscribe({
      next: res => {
        this.applications.set(res.data.content);
        this.loadingApps.set(false);
      },
      error: () => this.loadingApps.set(false)
    });
  }

  updateStatus(appId: string, newStatus: string): void {
    this.updatingId.set(appId);
    this.api.patch<ApplicationDto>(`/api/applications/${appId}/status`, { status: newStatus }).subscribe({
      next: res => {
        this.applications.update(list => list.map(a => a.id === appId ? res.data : a));
        this.updatingId.set(null);
      },
      error: () => {
        this.errorMsg.set('Failed to update status.');
        this.updatingId.set(null);
      }
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
