import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const noAuthGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  if (!authService.isAuthenticated()) return true;
  // Redirect to the right dashboard based on role
  const role = authService.userRole();
  const dashboardMap: Record<string, string> = {
    STUDENT: '/student/dashboard',
    FACULTY_MENTOR: '/faculty/dashboard',
    PLACEMENT_OFFICER: '/officer/dashboard',
    EMPLOYER: '/employer/dashboard',
    ADMIN: '/admin/dashboard'
  };
  router.navigate([dashboardMap[role ?? ''] ?? '/auth/login']);
  return false;
};
