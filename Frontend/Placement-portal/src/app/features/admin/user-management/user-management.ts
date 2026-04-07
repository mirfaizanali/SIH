import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule } from '@angular/material/paginator';
import { ApiService } from '../../../core/services/api.service';
import { PagedResponse } from '../../../core/models/api.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface AdminUserDto {
  id: string;
  email: string;
  fullName: string;
  role: string;
  isActive: boolean;
  createdAt: string;
}

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatPaginatorModule, LoadingSpinnerComponent],
  templateUrl: './user-management.html',
  styleUrl: './user-management.css'
})
export class UserManagement implements OnInit {
  private api = inject(ApiService);

  users = signal<AdminUserDto[]>([]);
  loading = signal(true);
  currentPage = signal(0);
  totalPages = signal(1);
  totalElements = signal(0);
  pageSize = 20;
  filterRole = signal('');
  filterActive = signal('');
  processingId = signal<string | null>(null);
  resetPasswordUserId = signal<string | null>(null);
  newPassword = signal('');
  successMsg = signal('');
  errorMsg = signal('');

  roles = ['STUDENT', 'FACULTY_MENTOR', 'EMPLOYER', 'PLACEMENT_OFFICER', 'ADMIN'];

  showAddForm = signal(false);
  creating = signal(false);
  newUser = signal({ fullName: '', email: '', password: '', role: 'STUDENT' });

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    const params: Record<string, string | number> = {
      page: this.currentPage(),
      size: this.pageSize
    };
    if (this.filterRole()) params['role'] = this.filterRole();
    if (this.filterActive() !== '') params['isActive'] = this.filterActive();

    this.api.get<PagedResponse<AdminUserDto>>('/api/admin/users', params).subscribe({
      next: res => {
        this.users.set(res.data.content);
        this.totalPages.set(res.data.totalPages);
        this.totalElements.set(res.data.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  applyFilters(): void {
    this.currentPage.set(0);
    this.loadUsers();
  }

  clearFilters(): void {
    this.filterRole.set('');
    this.filterActive.set('');
    this.currentPage.set(0);
    this.loadUsers();
  }

  toggleActive(user: AdminUserDto): void {
    const action = user.isActive ? 'deactivate' : 'activate';
    this.processingId.set(user.id);
    this.api.patch<AdminUserDto>(`/api/admin/users/${user.id}/${action}`).subscribe({
      next: res => {
        this.users.update(list => list.map(u => u.id === user.id ? res.data : u));
        this.processingId.set(null);
        this.successMsg.set(`User ${action}d successfully.`);
        setTimeout(() => this.successMsg.set(''), 3000);
      },
      error: () => {
        this.errorMsg.set(`Failed to ${action} user.`);
        this.processingId.set(null);
      }
    });
  }

  openResetPassword(userId: string): void {
    this.resetPasswordUserId.set(userId);
    this.newPassword.set('');
    this.errorMsg.set('');
  }

  submitResetPassword(): void {
    const userId = this.resetPasswordUserId();
    if (!userId || !this.newPassword()) {
      this.errorMsg.set('Please enter a new password.');
      return;
    }
    this.processingId.set(userId);
    this.api.post<void>(`/api/admin/users/${userId}/reset-password`, { newPassword: this.newPassword() }).subscribe({
      next: () => {
        this.resetPasswordUserId.set(null);
        this.processingId.set(null);
        this.successMsg.set('Password reset successfully.');
        setTimeout(() => this.successMsg.set(''), 3000);
      },
      error: () => {
        this.errorMsg.set('Failed to reset password.');
        this.processingId.set(null);
      }
    });
  }

  onPageChange(event: any): void {
    this.currentPage.set(event.pageIndex);
    this.loadUsers();
  }

  updateNewUser(field: string, value: string): void {
    this.newUser.update(f => ({ ...f, [field]: value }));
  }

  submitCreateUser(): void {
    const form = this.newUser();
    if (!form.fullName || !form.email || !form.password || !form.role) {
      this.errorMsg.set('Please fill all fields.');
      return;
    }
    this.creating.set(true);
    this.errorMsg.set('');
    this.api.post<AdminUserDto>('/api/admin/users', form).subscribe({
      next: res => {
        this.users.update(list => [res.data, ...list]);
        this.totalElements.update(n => n + 1);
        this.creating.set(false);
        this.showAddForm.set(false);
        this.newUser.set({ fullName: '', email: '', password: '', role: 'STUDENT' });
        this.successMsg.set('User created successfully.');
        setTimeout(() => this.successMsg.set(''), 3000);
      },
      error: (err) => {
        this.errorMsg.set(err?.error?.message ?? 'Failed to create user.');
        this.creating.set(false);
      }
    });
  }

  roleBadgeClass(role: string): string {
    const map: Record<string, string> = {
      'ADMIN': 'badge badge-danger',
      'PLACEMENT_OFFICER': 'badge badge-warning',
      'FACULTY_MENTOR': 'badge badge-info',
      'EMPLOYER': 'badge badge-neutral',
      'STUDENT': 'badge badge-success'
    };
    return map[role] ?? 'badge badge-neutral';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }
}
