import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../../core/services/api.service';
import { JobDto } from '../../../core/models/job.model';

@Component({
  selector: 'app-post-job',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './post-job.html',
  styleUrl: './post-job.css'
})
export class PostJob {
  private api = inject(ApiService);

  submitting = signal(false);
  successMsg = signal('');
  errorMsg = signal('');

  form = signal({
    title: '',
    description: '',
    location: '',
    jobType: 'FULL_TIME',
    experienceLevel: 'FRESHER',
    minCgpa: 6.0,
    salaryMin: 0,
    salaryMax: 0,
    openingsCount: 1,
    applicationDeadline: ''
  });

  jobTypes = ['FULL_TIME', 'PART_TIME', 'CONTRACT'];
  experienceLevels = ['FRESHER', 'JUNIOR', 'MID', 'SENIOR'];

  updateForm(field: string, value: string | number): void {
    this.form.update(f => ({ ...f, [field]: value }));
  }

  submit(): void {
    const f = this.form();
    if (!f.title || !f.description || !f.location || !f.applicationDeadline) {
      this.errorMsg.set('Please fill all required fields.');
      return;
    }
    this.submitting.set(true);
    this.errorMsg.set('');
    this.api.post<JobDto>('/api/jobs', f).subscribe({
      next: () => {
        this.submitting.set(false);
        this.successMsg.set('Job posted successfully!');
        this.form.set({
          title: '',
          description: '',
          location: '',
          jobType: 'FULL_TIME',
          experienceLevel: 'FRESHER',
          minCgpa: 6.0,
          salaryMin: 0,
          salaryMax: 0,
          openingsCount: 1,
          applicationDeadline: ''
        });
        setTimeout(() => this.successMsg.set(''), 5000);
      },
      error: () => {
        this.errorMsg.set('Failed to post job. Please try again.');
        this.submitting.set(false);
      }
    });
  }
}
