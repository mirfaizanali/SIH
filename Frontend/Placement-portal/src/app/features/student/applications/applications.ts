import { Component, OnInit, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule } from '@angular/material/paginator';
import { ApiService } from '../../../core/services/api.service';
import { ApplicationDto } from '../../../core/models/application.model';
import { PagedResponse } from '../../../core/models/api.model';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

@Component({
  selector: 'app-student-applications',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatPaginatorModule, StatusBadgeComponent, LoadingSpinnerComponent],
  templateUrl: './applications.html',
  styleUrl: './applications.css'
})
export class Applications implements OnInit {
  private api = inject(ApiService);

  applications = signal<ApplicationDto[]>([]);
  loading = signal(true);
  currentPage = signal(0);
  totalPages = signal(1);
  totalElements = signal(0);
  pageSize = 10;
  withdrawing = signal<string | null>(null);
  errorMsg = signal('');

  ngOnInit(): void {
    this.loadApplications();
  }

  loadApplications(): void {
    this.loading.set(true);
    this.api.get<PagedResponse<ApplicationDto>>('/api/applications/my', {
      page: this.currentPage(),
      size: this.pageSize
    }).subscribe({
      next: res => {
        this.applications.set(res.data.content);
        this.totalPages.set(res.data.totalPages);
        this.totalElements.set(res.data.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Failed to load applications.');
        this.loading.set(false);
      }
    });
  }

  withdraw(id: string): void {
    if (!confirm('Withdraw this application?')) return;
    this.withdrawing.set(id);
    this.api.delete<void>(`/api/applications/${id}`).subscribe({
      next: () => {
        this.applications.update(list => list.filter(a => a.id !== id));
        this.totalElements.update(n => n - 1);
        this.withdrawing.set(null);
      },
      error: () => {
        this.errorMsg.set('Failed to withdraw application.');
        this.withdrawing.set(null);
      }
    });
  }

  onPageChange(event: any): void {
    this.currentPage.set(event.pageIndex);
    this.loadApplications();
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
