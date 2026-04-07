import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ApiService } from '../../../core/services/api.service';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner';

interface ConfigDto {
  key: string;
  value: string;
  description?: string;
}

@Component({
  selector: 'app-system-config',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatButtonModule, MatFormFieldModule, MatInputModule, LoadingSpinnerComponent],
  templateUrl: './system-config.html',
  styleUrl: './system-config.css'
})
export class SystemConfig implements OnInit {
  private api = inject(ApiService);

  configs = signal<ConfigDto[]>([]);
  loading = signal(true);
  editingKey = signal<string | null>(null);
  editValue = signal('');
  savingKey = signal<string | null>(null);
  successMsg = signal('');
  errorMsg = signal('');

  ngOnInit(): void {
    this.loadConfigs();
  }

  loadConfigs(): void {
    this.api.get<ConfigDto[]>('/api/admin/configs').subscribe({
      next: res => {
        const data = Array.isArray(res.data) ? res.data : [];
        this.configs.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  startEdit(config: ConfigDto): void {
    this.editingKey.set(config.key);
    this.editValue.set(config.value);
  }

  cancelEdit(): void {
    this.editingKey.set(null);
    this.editValue.set('');
  }

  saveConfig(key: string): void {
    this.savingKey.set(key);
    this.errorMsg.set('');
    this.api.put<ConfigDto>(`/api/admin/configs/${key}`, { value: this.editValue() }).subscribe({
      next: res => {
        this.configs.update(list => list.map(c => c.key === key ? res.data : c));
        this.editingKey.set(null);
        this.savingKey.set(null);
        this.successMsg.set(`Config "${key}" saved.`);
        setTimeout(() => this.successMsg.set(''), 3000);
      },
      error: () => {
        this.errorMsg.set(`Failed to save config "${key}".`);
        this.savingKey.set(null);
      }
    });
  }
}
