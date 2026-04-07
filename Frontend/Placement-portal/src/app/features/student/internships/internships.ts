import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SlicePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../../core/services/api.service';
import { InternshipDto } from '../../../core/models/job.model';
import { ResumeDto } from '../../../core/models/student.model';
import { PagedResponse } from '../../../core/models/api.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';

@Component({
  selector: 'app-student-internships',
  standalone: true,
  imports: [FormsModule, SlicePipe, MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, MatSelectModule, LoadingSpinnerComponent, StatusBadgeComponent],
  templateUrl: './internships.html',
  styleUrl: './internships.css'
})
export class Internships implements OnInit {
  private api = inject(ApiService);

  internships = signal<InternshipDto[]>([]);
  resumes = signal<ResumeDto[]>([]);
  loading = signal(true);
  applying = signal(false);
  appliedIds = signal<Set<string>>(new Set());
  selectedInternship = signal<InternshipDto | null>(null);
  applyForm = signal({ internshipId: '', resumeId: '', coverLetter: '' });
  applyError = signal('');
  applySuccess = signal('');

  ngOnInit(): void {
    this.api.get<PagedResponse<InternshipDto>>('/api/internships', { page: 0, size: 20 }).subscribe({
      next: res => {
        this.internships.set(res.data.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
    this.api.get<ResumeDto[]>('/api/resumes/my').subscribe({
      next: res => this.resumes.set(res.data)
    });
    this.api.get<PagedResponse<{ internshipId: string }>>('/api/applications/my', { page: 0, size: 100 }).subscribe({
      next: res => {
        const ids = new Set(res.data.content.map(a => a.internshipId).filter(Boolean));
        this.appliedIds.set(ids);
      }
    });
  }

  openApply(internship: InternshipDto): void {
    this.selectedInternship.set(internship);
    this.applyForm.set({ internshipId: internship.id, resumeId: '', coverLetter: '' });
    this.applyError.set('');
    this.applySuccess.set('');
  }

  closeModal(): void {
    this.selectedInternship.set(null);
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
        const id = this.applyForm().internshipId;
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

  isApplied(id: string): boolean {
    return this.appliedIds().has(id);
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
