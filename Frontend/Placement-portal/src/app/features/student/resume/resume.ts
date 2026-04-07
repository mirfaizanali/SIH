import { Component, OnInit, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../../../core/services/api.service';
import { ResumeDto } from '../../../core/models/student.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

@Component({
  selector: 'app-student-resume',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatIconModule, LoadingSpinnerComponent],
  templateUrl: './resume.html',
  styleUrl: './resume.css'
})
export class Resume implements OnInit {
  private api = inject(ApiService);

  resumes = signal<ResumeDto[]>([]);
  loading = signal(true);
  uploading = signal(false);
  errorMsg = signal('');
  successMsg = signal('');

  ngOnInit(): void {
    this.loadResumes();
  }

  loadResumes(): void {
    this.api.get<ResumeDto[]>('/api/resumes/my').subscribe({
      next: res => {
        this.resumes.set(res.data);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Failed to load resumes.');
        this.loading.set(false);
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    const fd = new FormData();
    fd.append('file', file);
    this.uploading.set(true);
    this.errorMsg.set('');
    this.api.postForm<ResumeDto>('/api/resumes', fd).subscribe({
      next: res => {
        this.resumes.update(list => [...list, res.data]);
        this.uploading.set(false);
        this.successMsg.set('Resume uploaded successfully!');
        setTimeout(() => this.successMsg.set(''), 3000);
        input.value = '';
      },
      error: () => {
        this.errorMsg.set('Failed to upload resume.');
        this.uploading.set(false);
      }
    });
  }

  setPrimary(id: string): void {
    this.api.patch<ResumeDto>(`/api/resumes/${id}/primary`).subscribe({
      next: () => this.loadResumes()
    });
  }

  deleteResume(id: string): void {
    if (!confirm('Delete this resume?')) return;
    this.api.delete<void>(`/api/resumes/${id}`).subscribe({
      next: () => this.resumes.update(list => list.filter(r => r.id !== id))
    });
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
