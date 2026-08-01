import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';

import { AuthService } from '../services/auth.service';

const SKIP_URLS = ['/api/v1/auth/login', '/api/v1/auth/refresh-token'];

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  const shouldSkip = SKIP_URLS.some((url) => req.url.includes(url));
  if (shouldSkip) {
    return next(req);
  }

  if (!req.url.includes('/api/')) {
    return next(req);
  }

  const token = authService.getAccessToken();
  if (!token) {
    return next(req);
  }

  const authReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(authReq);
};
