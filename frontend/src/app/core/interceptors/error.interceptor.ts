import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';
import { ErrorMessageService } from '../services/error-message.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const errorMessageService = inject(ErrorMessageService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authService.clearSession();
        router.navigate(['/auth/login']);
      }

      const serverMessage = error.error?.error?.message;
      const message =
        serverMessage || errorMessageService.getHttpErrorMessage(error.status);

      const mappedError = {
        status: error.status,
        message,
        code: error.error?.error?.code || `HTTP_${error.status}`,
      };

      return throwError(() => mappedError);
    })
  );
};
