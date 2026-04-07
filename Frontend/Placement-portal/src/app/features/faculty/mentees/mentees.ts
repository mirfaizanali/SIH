import { Component, OnInit, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ApiService } from '../../../core/services/api.service';
import { StudentProfileDto } from '../../../core/models/student.model';
import { InternshipEnrollmentDto } from '../../../core/models/report.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface MenteeWithDetails extends StudentProfileDto {
  expanded: boolean;
  enrollments: InternshipEnrollmentDto[];
  loadingEnrollments: boolean;
}

@Component({
  selector: 'app-faculty-mentees',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, StatusBadgeComponent, LoadingSpinnerComponent],
  templateUrl: './mentees.html',
  styleUrl: './mentees.css'
})
export class Mentees implements OnInit {
  private api = inject(ApiService);

  mentees = signal<MenteeWithDetails[]>([]);
  loading = signal(true);
  searchQuery = signal('');

  ngOnInit(): void {
    this.api.get<StudentProfileDto[]>('/api/faculty/me/mentees').subscribe({
      next: res => {
        const data = Array.isArray(res.data) ? res.data : [];
        this.mentees.set(data.map(m => ({ ...m, expanded: false, enrollments: [], loadingEnrollments: false })));
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  toggleExpand(menteeId: string): void {
    this.mentees.update(list => list.map(m => {
      if (m.id !== menteeId) return m;
      if (!m.expanded && m.enrollments.length === 0) {
        this.loadEnrollments(menteeId);
      }
      return { ...m, expanded: !m.expanded };
    }));
  }

  loadEnrollments(studentId: string): void {
    this.mentees.update(list => list.map(m =>
      m.id === studentId ? { ...m, loadingEnrollments: true } : m
    ));
    this.api.get<InternshipEnrollmentDto[]>(`/api/enrollments/student/${studentId}`).subscribe({
      next: res => {
        const enrollments = Array.isArray(res.data) ? res.data : [];
        this.mentees.update(list => list.map(m =>
          m.id === studentId ? { ...m, enrollments, loadingEnrollments: false } : m
        ));
      },
      error: () => {
        this.mentees.update(list => list.map(m =>
          m.id === studentId ? { ...m, loadingEnrollments: false } : m
        ));
      }
    });
  }

  updateSearch(value: string): void {
    this.searchQuery.set(value);
  }

  get filteredMentees(): MenteeWithDetails[] {
    const q = this.searchQuery().toLowerCase();
    if (!q) return this.mentees();
    return this.mentees().filter(m =>
      m.fullName.toLowerCase().includes(q) ||
      m.rollNumber.toLowerCase().includes(q) ||
      m.department.toLowerCase().includes(q)
    );
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
