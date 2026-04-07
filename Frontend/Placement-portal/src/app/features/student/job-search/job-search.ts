import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule } from '@angular/material/paginator';
import { ApiService } from '../../../core/services/api.service';
import { JobDto } from '../../../core/models/job.model';
import { ResumeDto } from '../../../core/models/student.model';
import { PagedResponse } from '../../../core/models/api.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface ApplyForm {
  jobId: string;
  resumeId: string;
  coverLetter: string;
}

@Component({
  selector: 'app-job-search',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatPaginatorModule, LoadingSpinnerComponent],
  templateUrl: './job-search.html',
  styleUrl: './job-search.css'
})
export class JobSearch implements OnInit {
  private api = inject(ApiService);

  jobs = signal<JobDto[]>([]);
  resumes = signal<ResumeDto[]>([]);
  loading = signal(true);
  applying = signal(false);

  filterLocation = signal('');
  filterLevel = signal('');
  currentPage = signal(0);
  totalPages = signal(1);
  totalElements = signal(0);
  pageSize = 12;

  appliedJobIds = signal<Set<string>>(new Set());
  selectedJobForApply = signal<JobDto | null>(null);
  applyForm = signal<ApplyForm>({ jobId: '', resumeId: '', coverLetter: '' });
  applyError = signal('');
  applySuccess = signal('');

  experienceLevels = ['FRESHER', 'JUNIOR', 'MID', 'SENIOR'];

  ngOnInit(): void {
    this.loadJobs();
    this.api.get<ResumeDto[]>('/api/resumes/my').subscribe({
      next: res => this.resumes.set(res.data)
    });
    this.api.get<PagedResponse<{ jobId: string }>>('/api/applications/my', { page: 0, size: 100 }).subscribe({
      next: res => {
        const ids = new Set(res.data.content.map(a => a.jobId).filter(Boolean));
        this.appliedJobIds.set(ids);
      }
    });
  }

  loadJobs(): void {
    this.loading.set(true);
    const params: Record<string, string | number> = { page: this.currentPage(), size: this.pageSize };
    if (this.filterLocation()) params['location'] = this.filterLocation();
    if (this.filterLevel()) params['experienceLevel'] = this.filterLevel();

    this.api.get<PagedResponse<JobDto>>('/api/jobs', params).subscribe({
      next: res => {
        this.jobs.set(res.data.content);
        this.totalPages.set(res.data.totalPages);
        this.totalElements.set(res.data.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  applyFilter(): void {
    this.currentPage.set(0);
    this.loadJobs();
  }

  clearFilter(): void {
    this.filterLocation.set('');
    this.filterLevel.set('');
    this.currentPage.set(0);
    this.loadJobs();
  }

  onPageChange(event: any): void {
    this.currentPage.set(event.pageIndex);
    this.loadJobs();
  }

  openApplyModal(job: JobDto): void {
    this.selectedJobForApply.set(job);
    this.applyForm.set({ jobId: job.id, resumeId: '', coverLetter: '' });
    this.applyError.set('');
    this.applySuccess.set('');
  }

  closeModal(): void {
    this.selectedJobForApply.set(null);
  }

  updateApplyForm(field: string, value: string): void {
    this.applyForm.update(f => ({ ...f, [field]: value }));
  }

  submitApplication(): void {
    if (!this.applyForm().resumeId) {
      this.applyError.set('Please select a resume.');
      return;
    }
    this.applying.set(true);
    this.api.post<void>('/api/applications', this.applyForm()).subscribe({
      next: () => {
        const jobId = this.applyForm().jobId;
        this.appliedJobIds.update(ids => { ids.add(jobId); return new Set(ids); });
        this.applying.set(false);
        this.applySuccess.set('Application submitted successfully!');
        setTimeout(() => this.closeModal(), 1500);
      },
      error: () => {
        this.applyError.set('Failed to submit application.');
        this.applying.set(false);
      }
    });
  }

  formatSalary(min: number, max: number): string {
    if (!min && !max) return 'Not disclosed';
    return `₹${(min / 100000).toFixed(1)}L – ₹${(max / 100000).toFixed(1)}L`;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  isApplied(jobId: string): boolean {
    return this.appliedJobIds().has(jobId);
  }
}
