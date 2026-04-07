import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const required: string = route.data?.['requiredRole'];
  if (!required || authService.userRole() === required) return true;
  router.navigate(['/auth/login']);
  return false;
};
