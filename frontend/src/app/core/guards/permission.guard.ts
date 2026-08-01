import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';

import { PermissionService } from '../services/permission.service';
import { TablaPermisos } from '../models/auth.models';

/**
 * Guard funcional que verifica permisos del usuario sobre una tabla/acción.
 *
 * Uso en rutas:
 * ```typescript
 * {
 *   path: 'leads',
 *   component: LeadListComponent,
 *   canActivate: [permissionGuard],
 *   data: { tabla: 'lead', accion: 'ver_listado' }
 * }
 * ```
 */
export const permissionGuard: CanActivateFn = (route) => {
  const permissionService = inject(PermissionService);

  const tabla = route.data['tabla'] as string | undefined;
  const accion = route.data['accion'] as keyof TablaPermisos | undefined;

  if (!tabla || !accion) {
    return false;
  }

  return permissionService.hasPermission(tabla, accion);
};
