import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { NavbarComponent } from '../../shared/components/navbar/navbar';
import { SidebarComponent, NavItem } from '../../shared/components/sidebar/sidebar';

@Component({
  selector: 'app-student-layout',
  standalone: true,
  imports: [NavbarComponent, SidebarComponent, RouterOutlet, MatSidenavModule],
  template: `
    <app-navbar />
    <mat-sidenav-container class="layout-body">
      <mat-sidenav mode="side" opened class="sidebar-nav">
        <app-sidebar [navItems]="navItems" />
      </mat-sidenav>
      <mat-sidenav-content class="main-content">
        <router-outlet />
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .layout-body { height: calc(100vh - 64px); }
    .sidebar-nav {
      width: 260px;
      background: var(--surface) !important;
      border-right: 1px solid var(--border) !important;
    }
    .main-content {
      padding: 2rem 2.5rem;
      overflow-y: auto;
      background: var(--bg);
    }
  `]
})
export class StudentLayout {
  navItems: NavItem[] = [
    { label: 'Dashboard',       icon: 'home',        route: '/student/dashboard' },
    { label: 'Job Search',      icon: 'search',      route: '/student/jobs' },
    { label: 'Applications',    icon: 'assignment',   route: '/student/applications' },
    { label: 'Internships',     icon: 'business',    route: '/student/internships' },
    { label: 'Recommendations', icon: 'star',        route: '/student/recommendations' },
    { label: 'Reports',         icon: 'description', route: '/student/reports' },
    { label: 'My Resume',       icon: 'folder',      route: '/student/resume' },
    { label: 'Profile',         icon: 'person',      route: '/student/profile' },
  ];
}
