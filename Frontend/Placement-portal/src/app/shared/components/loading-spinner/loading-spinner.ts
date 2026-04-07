import { Component } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  imports: [MatProgressSpinnerModule],
  template: `
    <div class="spinner-wrapper">
      <mat-spinner diameter="40"></mat-spinner>
      <p class="spinner-text">Loading...</p>
    </div>
  `,
  styles: [`
    .spinner-wrapper {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 3rem;
      gap: 1rem;
    }
    .spinner-text {
      color: var(--text-muted);
      font-size: 0.875rem;
    }
  `]
})
export class LoadingSpinnerComponent {}
