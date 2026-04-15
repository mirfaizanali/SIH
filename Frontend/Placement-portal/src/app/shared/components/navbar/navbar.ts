import { Component, inject } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ApiService } from '../../../core/services/api.service';
import { ThemeService } from '../../../core/services/theme.service';
import { NotificationBellComponent } from '../notification-bell/notification-bell';
import { StatusBadgeComponent } from '../status-badge/status-badge';
import { OnInit, signal } from '@angular/core';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [MatToolbarModule, MatIconModule, MatButtonModule, MatTooltipModule, MatMenuModule, MatDividerModule, RouterModule, NotificationBellComponent, StatusBadgeComponent],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent implements OnInit {
  authService = inject(AuthService);
  themeService = inject(ThemeService);
  apiService = inject(ApiService);

  studentMiniProfile = signal<any>(null);

  ngOnInit(): void {
    if (this.authService.userRole() === 'STUDENT') {
      this.apiService.get<any>('/api/students/me').subscribe({
        next: (res) => this.studentMiniProfile.set(res.data),
        error: () => console.warn('Failed to load mini profile')
      });
    }
  }
}
