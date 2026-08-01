import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

const HTTP_ERROR_MESSAGES: Record<number, string> = {
  400: 'La solicitud contiene datos inválidos',
  401: 'No tiene autorización. Inicie sesión nuevamente',
  403: 'No tiene permisos para realizar esta acción',
  404: 'El recurso solicitado no fue encontrado',
  409: 'Existe un conflicto con los datos actuales',
  422: 'Los datos enviados no pudieron ser procesados',
  500: 'Ocurrió un error en el servidor. Intente más tarde',
  503: 'El servicio no está disponible temporalmente',
  0: 'No se pudo conectar con el servidor. Verifique su conexión a internet',
};

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authService.clearSession();
        router.navigate(['/auth/login']);
      }

      const serverMessage = error.error?.error?.message;
      const message =
        serverMessage || HTTP_ERROR_MESSAGES[error.status] || HTTP_ERROR_MESSAGES[0];

      const mappedError = {
        status: error.status,
        message,
        code: error.error?.error?.code || `HTTP_${error.status}`,
      };

      return throwError(() => mappedError);
    })
  );
};
