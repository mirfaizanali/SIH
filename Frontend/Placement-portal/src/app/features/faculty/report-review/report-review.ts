import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ApiService } from '../../../core/services/api.service';
import { ReportDto } from '../../../core/models/report.model';
import { PagedResponse } from '../../../core/models/api.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

@Component({
  selector: 'app-faculty-report-review',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, StatusBadgeComponent, LoadingSpinnerComponent],
  templateUrl: './report-review.html',
  styleUrl: './report-review.css'
})
export class ReportReview implements OnInit {
  private api = inject(ApiService);

  activeTab = signal<'pending' | 'reviewed'>('pending');
  pendingReports = signal<ReportDto[]>([]);
  reviewedReports = signal<ReportDto[]>([]);
  loading = signal(true);
  expandedId = signal<string | null>(null);
  revisionReportId = signal<string | null>(null);
  revisionComment = signal('');
  processing = signal<string | null>(null);
  successMsg = signal('');
  errorMsg = signal('');

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.loading.set(true);
    this.api.get<PagedResponse<ReportDto>>('/api/reports', { status: 'SUBMITTED', page: 0, size: 50 }).subscribe({
      next: res => {
        this.pendingReports.set(res.data.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
    this.api.get<PagedResponse<ReportDto>>('/api/reports', { status: 'APPROVED', page: 0, size: 20 }).subscribe({
      next: res => this.reviewedReports.set(res.data.content)
    });
  }

  toggleExpand(id: string): void {
    this.expandedId.set(this.expandedId() === id ? null : id);
  }

  approve(id: string): void {
    if (!confirm('Approve this report?')) return;
    this.processing.set(id);
    this.api.patch<ReportDto>(`/api/reports/${id}`, { status: 'APPROVED', reviewerComments: '' }).subscribe({
      next: () => {
        this.pendingReports.update(list => {
          const report = list.find(r => r.id === id);
          if (report) this.reviewedReports.update(reviewed => [{ ...report, status: 'APPROVED' }, ...reviewed]);
          return list.filter(r => r.id !== id);
        });
        this.processing.set(null);
        this.successMsg.set('Report approved successfully!');
        setTimeout(() => this.successMsg.set(''), 3000);
      },
      error: () => {
        this.errorMsg.set('Failed to approve report.');
        this.processing.set(null);
      }
    });
  }

  openRevision(id: string): void {
    this.revisionReportId.set(id);
    this.revisionComment.set('');
  }

  submitRevision(): void {
    const id = this.revisionReportId();
    if (!id || !this.revisionComment()) {
      this.errorMsg.set('Please enter revision comments.');
      return;
    }
    this.processing.set(id);
    this.api.patch<ReportDto>(`/api/reports/${id}`, {
      status: 'REVISION_REQUESTED',
      reviewerComments: this.revisionComment()
    }).subscribe({
      next: () => {
        this.pendingReports.update(list => list.filter(r => r.id !== id));
        this.revisionReportId.set(null);
        this.processing.set(null);
        this.successMsg.set('Revision requested.');
        setTimeout(() => this.successMsg.set(''), 3000);
      },
      error: () => {
        this.errorMsg.set('Failed to request revision.');
        this.processing.set(null);
      }
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
