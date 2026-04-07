import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../../core/services/api.service';
import { ReportDto, InternshipEnrollmentDto } from '../../../core/models/report.model';
import { PagedResponse } from '../../../core/models/api.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

@Component({
  selector: 'app-student-reports',
  standalone: true,
  imports: [FormsModule, SlicePipe, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, StatusBadgeComponent, LoadingSpinnerComponent],
  templateUrl: './reports.html',
  styleUrl: './reports.css'
})
export class Reports implements OnInit {
  private api = inject(ApiService);

  reports = signal<ReportDto[]>([]);
  enrollments = signal<InternshipEnrollmentDto[]>([]);
  loading = signal(true);
  submitting = signal(false);
  showForm = signal(false);
  errorMsg = signal('');
  successMsg = signal('');

  reportTypes = ['WEEKLY', 'MONTHLY', 'FINAL'];

  newReport = signal({
    title: '',
    reportType: 'WEEKLY',
    content: '',
    enrollmentId: ''
  });

  ngOnInit(): void {
    this.loadReports();
    this.api.get<PagedResponse<InternshipEnrollmentDto>>('/api/enrollments/my', { page: 0, size: 20 }).subscribe({
      next: res => this.enrollments.set(res.data.content)
    });
  }

  loadReports(): void {
    this.api.get<PagedResponse<ReportDto>>('/api/reports/my', { page: 0, size: 20 }).subscribe({
      next: res => {
        this.reports.set(res.data.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  updateForm(field: string, value: string): void {
    this.newReport.update(f => ({ ...f, [field]: value }));
  }

  submitReport(): void {
    const form = this.newReport();
    if (!form.title || !form.content || !form.enrollmentId) {
      this.errorMsg.set('Please fill all required fields.');
      return;
    }
    this.submitting.set(true);
    this.errorMsg.set('');
    this.api.post<ReportDto>('/api/reports', form).subscribe({
      next: res => {
        this.reports.update(list => [res.data, ...list]);
        this.submitting.set(false);
        this.successMsg.set('Report submitted successfully!');
        this.showForm.set(false);
        this.newReport.set({ title: '', reportType: 'WEEKLY', content: '', enrollmentId: '' });
        setTimeout(() => this.successMsg.set(''), 3000);
      },
      error: () => {
        this.errorMsg.set('Failed to submit report.');
        this.submitting.set(false);
      }
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
