import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../../core/services/api.service';
import { ApplicationDto, InterviewDto } from '../../../core/models/application.model';
import { PagedResponse } from '../../../core/models/api.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

@Component({
  selector: 'app-employer-interviews',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, MatSelectModule, StatusBadgeComponent, LoadingSpinnerComponent],
  templateUrl: './interviews.html',
  styleUrl: './interviews.css'
})
export class Interviews implements OnInit {
  private api = inject(ApiService);

  shortlisted = signal<ApplicationDto[]>([]);
  interviews = signal<InterviewDto[]>([]);
  loading = signal(true);
  schedulingFor = signal<ApplicationDto | null>(null);
  scheduling = signal(false);
  successMsg = signal('');
  errorMsg = signal('');

  interviewForm = signal({
    applicationId: '',
    roundNumber: 1,
    interviewType: 'TECHNICAL',
    scheduledAt: '',
    durationMins: 60,
    meetingLink: '',
    location: ''
  });

  interviewTypes = ['TECHNICAL', 'HR', 'MANAGERIAL', 'GROUP_DISCUSSION'];

  ngOnInit(): void {
    this.api.get<PagedResponse<ApplicationDto>>('/api/applications/employer', {
      status: 'SHORTLISTED', page: 0, size: 50
    }).subscribe({
      next: res => {
        this.shortlisted.set(res.data.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });

    this.api.get<PagedResponse<InterviewDto>>('/api/interviews/employer', { page: 0, size: 50 }).subscribe({
      next: res => this.interviews.set(res.data.content)
    });
  }

  openSchedule(app: ApplicationDto): void {
    this.schedulingFor.set(app);
    this.interviewForm.set({
      applicationId: app.id,
      roundNumber: 1,
      interviewType: 'TECHNICAL',
      scheduledAt: '',
      durationMins: 60,
      meetingLink: '',
      location: ''
    });
    this.errorMsg.set('');
    this.successMsg.set('');
  }

  closeModal(): void {
    this.schedulingFor.set(null);
  }

  updateForm(field: string, value: string | number): void {
    this.interviewForm.update(f => ({ ...f, [field]: value }));
  }

  scheduleInterview(): void {
    const f = this.interviewForm();
    if (!f.scheduledAt) {
      this.errorMsg.set('Please select a date and time.');
      return;
    }
    this.scheduling.set(true);
    this.api.post<InterviewDto>('/api/interviews', f).subscribe({
      next: res => {
        this.interviews.update(list => [res.data, ...list]);
        this.scheduling.set(false);
        this.successMsg.set('Interview scheduled successfully!');
        setTimeout(() => this.closeModal(), 1500);
      },
      error: () => {
        this.errorMsg.set('Failed to schedule interview.');
        this.scheduling.set(false);
      }
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleString('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });
  }
}
