import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../../core/services/api.service';
import { PagedResponse } from '../../../core/models/api.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface EmployerDto {
  id: string;
  companyName: string;
  industry: string;
  companySize: string;
  hrContactName: string;
  hrContactPhone: string;
  location: string;
  isVerified: boolean;
  createdAt: string;
}

@Component({
  selector: 'app-employer-management',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, LoadingSpinnerComponent],
  templateUrl: './employer-management.html',
  styleUrl: './employer-management.css'
})
export class EmployerManagement implements OnInit {
  private api = inject(ApiService);

  employers = signal<EmployerDto[]>([]);
  verifiedCount = computed(() => this.employers().filter(e => e.isVerified).length);
  pendingCount = computed(() => this.employers().filter(e => !e.isVerified).length);
  loading = signal(true);
  verifyingId = signal<string | null>(null);
  successMsg = signal('');
  errorMsg = signal('');

  ngOnInit(): void {
    this.loadEmployers();
  }

  loadEmployers(): void {
    this.api.get<PagedResponse<EmployerDto>>('/api/employers', { page: 0, size: 50 }).subscribe({
      next: res => {
        this.employers.set(res.data.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  verify(id: string): void {
    this.verifyingId.set(id);
    this.api.put<EmployerDto>(`/api/employers/${id}/verify`, {}).subscribe({
      next: res => {
        this.employers.update(list => list.map(e => e.id === id ? { ...e, isVerified: true } : e));
        this.verifyingId.set(null);
        this.successMsg.set('Employer verified successfully!');
        setTimeout(() => this.successMsg.set(''), 3000);
      },
      error: () => {
        this.errorMsg.set('Failed to verify employer.');
        this.verifyingId.set(null);
      }
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
