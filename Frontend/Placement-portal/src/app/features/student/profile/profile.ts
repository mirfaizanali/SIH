import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../../../core/services/api.service';
import { StudentProfileDto } from '../../../core/models/student.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

@Component({
  selector: 'app-student-profile',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatIconModule, StatusBadgeComponent, LoadingSpinnerComponent],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {
  private api = inject(ApiService);

  profile = signal<StudentProfileDto | null>(null);
  loading = signal(true);
  saving = signal(false);
  saveSuccess = signal(false);
  errorMsg = signal('');

  editForm = signal({
    phone: '',
    linkedinUrl: '',
    githubUrl: '',
    bio: '',
    preferredLocations: '',
    preferredJobTypes: ''
  });

  ngOnInit(): void {
    this.api.get<StudentProfileDto>('/api/students/me').subscribe({
      next: res => {
        this.profile.set(res.data);
        this.editForm.set({
          phone: res.data.phone ?? '',
          linkedinUrl: res.data.linkedinUrl ?? '',
          githubUrl: res.data.githubUrl ?? '',
          bio: res.data.bio ?? '',
          preferredLocations: '',
          preferredJobTypes: ''
        });
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Failed to load profile.');
        this.loading.set(false);
      }
    });
  }

  updateField(field: string, value: string): void {
    this.editForm.update(f => ({ ...f, [field]: value }));
  }

  save(): void {
    this.saving.set(true);
    this.saveSuccess.set(false);
    this.api.put<StudentProfileDto>('/api/students/me', this.editForm()).subscribe({
      next: res => {
        this.profile.set(res.data);
        this.saving.set(false);
        this.saveSuccess.set(true);
        setTimeout(() => this.saveSuccess.set(false), 3000);
      },
      error: () => {
        this.errorMsg.set('Failed to save profile.');
        this.saving.set(false);
      }
    });
  }
}
