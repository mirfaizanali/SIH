import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule } from '@angular/material/paginator';
import { ApiService } from '../../../core/services/api.service';
import { PagedResponse } from '../../../core/models/api.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface AuditLogDto {
  id: string;
  userId: string;
  userEmail: string;
  action: string;
  entityType: string;
  entityId: string;
  ipAddress: string;
  timestamp: string;
  details?: string;
}

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatSelectModule, MatPaginatorModule, LoadingSpinnerComponent],
  templateUrl: './audit-logs.html',
  styleUrl: './audit-logs.css'
})
export class AuditLogs implements OnInit {
  private api = inject(ApiService);

  logs = signal<AuditLogDto[]>([]);
  loading = signal(true);
  currentPage = signal(0);
  totalPages = signal(1);
  totalElements = signal(0);
  pageSize = 25;
  filterAction = signal('');

  commonActions = ['LOGIN', 'LOGOUT', 'CREATE', 'UPDATE', 'DELETE', 'VIEW', 'VERIFY', 'UPLOAD'];

  ngOnInit(): void {
    this.loadLogs();
  }

  loadLogs(): void {
    this.loading.set(true);
    const params: Record<string, string | number> = {
      page: this.currentPage(),
      size: this.pageSize
    };
    if (this.filterAction()) params['action'] = this.filterAction();

    this.api.get<PagedResponse<AuditLogDto>>('/api/admin/audit-logs', params).subscribe({
      next: res => {
        this.logs.set(res.data.content);
        this.totalPages.set(res.data.totalPages);
        this.totalElements.set(res.data.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  applyFilter(): void {
    this.currentPage.set(0);
    this.loadLogs();
  }

  clearFilter(): void {
    this.filterAction.set('');
    this.currentPage.set(0);
    this.loadLogs();
  }

  onPageChange(event: any): void {
    this.currentPage.set(event.pageIndex);
    this.loadLogs();
  }

  actionBadgeClass(action: string): string {
    const a = action?.toUpperCase();
    if (['CREATE', 'UPLOAD'].includes(a)) return 'badge badge-success';
    if (['DELETE', 'DEACTIVATE'].includes(a)) return 'badge badge-danger';
    if (['LOGIN', 'LOGOUT'].includes(a)) return 'badge badge-info';
    if (['UPDATE', 'VERIFY'].includes(a)) return 'badge badge-warning';
    return 'badge badge-neutral';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleString('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    });
  }
}
