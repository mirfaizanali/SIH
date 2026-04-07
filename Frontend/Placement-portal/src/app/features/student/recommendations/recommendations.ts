import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../../core/services/api.service';
import { JobDto } from '../../../core/models/job.model';
import { ResumeDto } from '../../../core/models/student.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

@Component({
  selector: 'app-student-recommendations',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, MatSelectModule, LoadingSpinnerComponent],
  templateUrl: './recommendations.html',
  styleUrl: './recommendations.css'
})
export class Recommendations implements OnInit {
  private api = inject(ApiService);

  jobs = signal<JobDto[]>([]);
  resumes = signal<ResumeDto[]>([]);
  loading = signal(true);
  applying = signal(false);
  selectedJob = signal<JobDto | null>(null);
  applyForm = signal({ jobId: '', resumeId: '', coverLetter: '' });
  applyError = signal('');
  applySuccess = signal('');
  appliedIds = signal<Set<string>>(new Set());

  ngOnInit(): void {
    this.api.get<JobDto[]>('/api/recommendations/jobs', { limit: 10 }).subscribe({
      next: res => {
        this.jobs.set(res.data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
    this.api.get<ResumeDto[]>('/api/resumes/my').subscribe({
      next: res => this.resumes.set(res.data)
    });
  }

  matchBadgeClass(score: number): string {
    if (score >= 75) return 'match-badge match-high';
    if (score >= 50) return 'match-badge match-medium';
    return 'match-badge match-low';
  }

  openApply(job: JobDto): void {
    this.selectedJob.set(job);
    this.applyForm.set({ jobId: job.id, resumeId: '', coverLetter: '' });
    this.applyError.set('');
    this.applySuccess.set('');
  }

  closeModal(): void {
    this.selectedJob.set(null);
  }

  updateForm(field: string, value: string): void {
    this.applyForm.update(f => ({ ...f, [field]: value }));
  }

  submitApply(): void {
    if (!this.applyForm().resumeId) {
      this.applyError.set('Please select a resume.');
      return;
    }
    this.applying.set(true);
    this.api.post<void>('/api/applications', this.applyForm()).subscribe({
      next: () => {
        const id = this.applyForm().jobId;
        this.appliedIds.update(s => { s.add(id); return new Set(s); });
        this.applying.set(false);
        this.applySuccess.set('Application submitted!');
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
}
