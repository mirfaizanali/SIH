import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { NavbarComponent } from '../../shared/components/navbar/navbar';
import { SidebarComponent, NavItem } from '../../shared/components/sidebar/sidebar';

@Component({
  selector: 'app-officer-layout',
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
    .layout-body { height: calc(100vh - 56px); }
    .sidebar-nav {
      width: 220px;
      background: var(--color-bg-surface) !important;
      border-right: 1px solid var(--color-border) !important;
    }
    .main-content {
      padding: 32px 32px 32px 24px;
      overflow-y: auto;
      background: var(--color-bg-page);
    }
  `]
})
export class OfficerLayout {
  navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'home',         route: '/officer/dashboard' },
    { label: 'Drives',    icon: 'track_changes', route: '/officer/drives' },
    { label: 'Employers', icon: 'business',     route: '/officer/employers' },
    { label: 'Students',  icon: 'school',       route: '/officer/students' },
    { label: 'Analytics', icon: 'bar_chart',    route: '/officer/analytics' },
  ];
}
