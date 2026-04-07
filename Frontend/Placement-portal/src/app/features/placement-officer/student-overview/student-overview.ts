import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule } from '@angular/material/paginator';
import { ApiService } from '../../../core/services/api.service';
import { StudentProfileDto } from '../../../core/models/student.model';
import { PagedResponse } from '../../../core/models/api.model';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

@Component({
  selector: 'app-student-overview',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatPaginatorModule, LoadingSpinnerComponent],
  templateUrl: './student-overview.html',
  styleUrl: './student-overview.css'
})
export class StudentOverview implements OnInit {
  private api = inject(ApiService);

  students = signal<StudentProfileDto[]>([]);
  loading = signal(true);
  currentPage = signal(0);
  totalPages = signal(1);
  totalElements = signal(0);
  pageSize = 20;

  filterDept = signal('');
  filterBatch = signal('');
  filterPlaced = signal('');

  ngOnInit(): void {
    this.loadStudents();
  }

  loadStudents(): void {
    this.loading.set(true);
    const params: Record<string, string | number> = {
      page: this.currentPage(),
      size: this.pageSize
    };
    if (this.filterDept()) params['department'] = this.filterDept();
    if (this.filterBatch()) params['batchYear'] = this.filterBatch();
    if (this.filterPlaced() !== '') params['isPlaced'] = this.filterPlaced();

    this.api.get<PagedResponse<StudentProfileDto>>('/api/students', params).subscribe({
      next: res => {
        this.students.set(res.data.content);
        this.totalPages.set(res.data.totalPages);
        this.totalElements.set(res.data.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  applyFilters(): void {
    this.currentPage.set(0);
    this.loadStudents();
  }

  clearFilters(): void {
    this.filterDept.set('');
    this.filterBatch.set('');
    this.filterPlaced.set('');
    this.currentPage.set(0);
    this.loadStudents();
  }

  onPageChange(event: any): void {
    this.currentPage.set(event.pageIndex);
    this.loadStudents();
  }
}
