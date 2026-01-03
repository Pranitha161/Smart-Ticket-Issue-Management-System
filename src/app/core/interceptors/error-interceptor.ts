import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Toast } from '../services/toast';
import { BYPASS_LOGIC } from '../constants/tokens';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(Toast);
  if (req.context.get(BYPASS_LOGIC)) {
    return next(req);
  }

  return next(req).pipe(
    catchError((error) => {
      const errorMessage = error.error?.message || 'An error occurred';
      toastService.show(errorMessage, 'error');
      return throwError(() => error);
    })
  );
};