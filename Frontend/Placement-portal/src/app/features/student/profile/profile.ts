import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { ApiService } from '../../../core/services/api.service';
import { StudentProfileDto } from '../../../core/models/student.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

@Component({
  selector: 'app-student-profile',
  standalone: true,
  imports: [
    FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, 
    MatSelectModule, MatButtonModule, MatIconModule, MatDialogModule,
    StatusBadgeComponent, LoadingSpinnerComponent
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {
  private api = inject(ApiService);
  private dialog = inject(MatDialog);

  profile = signal<StudentProfileDto | null>(null);
  loading = signal(true);
  saving = signal(false);
  saveSuccess = signal(false);
  errorMsg = signal('');
  dialogRef: MatDialogRef<any> | null = null;

  editForm = signal({
    rollNumber: '',
    department: '',
    batchYear: null as number | null,
    cgpa: null as number | null,
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
          rollNumber: res.data.rollNumber ?? '',
          department: res.data.department ?? '',
          batchYear: res.data.batchYear ?? null,
          cgpa: res.data.cgpa ?? null,
          phone: res.data.phone ?? '',
          linkedinUrl: res.data.linkedinUrl ?? '',
          githubUrl: res.data.githubUrl ?? '',
          bio: res.data.bio ?? '',
          preferredLocations: res.data.preferredLocations ?? '',
          preferredJobTypes: res.data.preferredJobTypes ?? ''
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

  openEditDialog(template: any): void {
    // Reset edit form to match current profile when opening modal
    const current = this.profile();
    if(current) {
      this.editForm.set({
        rollNumber: current.rollNumber ?? '',
        department: current.department ?? '',
        batchYear: current.batchYear ?? null,
        cgpa: current.cgpa ?? null,
        phone: current.phone ?? '',
        linkedinUrl: current.linkedinUrl ?? '',
        githubUrl: current.githubUrl ?? '',
        bio: current.bio ?? '',
        preferredLocations: current.preferredLocations ?? '',
        preferredJobTypes: current.preferredJobTypes ?? ''
      });
    }
    this.saveSuccess.set(false);
    this.errorMsg.set('');
    this.dialogRef = this.dialog.open(template, {
      width: '800px',
      maxWidth: '90vw',
      disableClose: true
    });
  }

  closeDialog(): void {
    if (this.dialogRef) {
      this.dialogRef.close();
      this.dialogRef = null;
    }
  }

  save(): void {
    this.saving.set(true);
    this.saveSuccess.set(false);
    this.api.put<StudentProfileDto>('/api/students/me', this.editForm()).subscribe({
      next: res => {
        this.profile.set(res.data);
        this.saving.set(false);
        this.saveSuccess.set(true);
        this.closeDialog();
        // Optional quick banner in background
        setTimeout(() => this.saveSuccess.set(false), 3000);
      },
      error: () => {
        this.errorMsg.set('Failed to save profile.');
        this.saving.set(false);
      }
    });
  }
}
