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
import { PagedResponse } from '../../../core/models/api.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface DriveDto {
  id: string;
  title: string;
  driveDate: string;
  venue: string;
  mode: string;
  description: string;
  status: string;
  createdAt: string;
}

@Component({
  selector: 'app-officer-drives',
  standalone: true,
  imports: [FormsModule, SlicePipe, MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, MatSelectModule, StatusBadgeComponent, LoadingSpinnerComponent],
  templateUrl: './drives.html',
  styleUrl: './drives.css'
})
export class Drives implements OnInit {
  private api = inject(ApiService);

  drives = signal<DriveDto[]>([]);
  loading = signal(true);
  showForm = signal(false);
  submitting = signal(false);
  successMsg = signal('');
  errorMsg = signal('');

  modes = ['ON_CAMPUS', 'OFF_CAMPUS', 'VIRTUAL'];

  form = signal({
    title: '',
    driveDate: '',
    venue: '',
    mode: 'ON_CAMPUS',
    description: ''
  });

  ngOnInit(): void {
    this.loadDrives();
  }

  loadDrives(): void {
    this.api.get<PagedResponse<DriveDto>>('/api/drives', { page: 0, size: 20 }).subscribe({
      next: res => {
        this.drives.set(res.data.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  updateForm(field: string, value: string): void {
    this.form.update(f => ({ ...f, [field]: value }));
  }

  submit(): void {
    const f = this.form();
    if (!f.title || !f.driveDate) {
      this.errorMsg.set('Please fill all required fields.');
      return;
    }
    this.submitting.set(true);
    this.errorMsg.set('');
    this.api.post<DriveDto>('/api/drives', f).subscribe({
      next: res => {
        this.drives.update(list => [res.data, ...list]);
        this.submitting.set(false);
        this.successMsg.set('Drive created successfully!');
        this.showForm.set(false);
        this.form.set({ title: '', driveDate: '', venue: '', mode: 'ON_CAMPUS', description: '' });
        setTimeout(() => this.successMsg.set(''), 3000);
      },
      error: () => {
        this.errorMsg.set('Failed to create drive.');
        this.submitting.set(false);
      }
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
