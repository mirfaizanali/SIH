import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 403) {
        console.error('Access denied');
      } else if (error.status === 0) {
        console.error('Network error - backend unreachable');
      } else if (error.status >= 500) {
        console.error('Server error', error.message);
      }
      return throwError(() => error);
    })
  );
};
