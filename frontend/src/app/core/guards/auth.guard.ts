import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import {map, catchError, of} from 'rxjs';

import { AuthService } from '../services/auth.service';
import { PermissionService } from '../services/permission.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const permissionService = inject(PermissionService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/auth/login']);
  }

  return permissionService.loadPermisos().pipe(
    map(() => true),
    catchError(() => of(true))
  )
};
