import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { permissionGuard } from './core/guards/permission.guard';

export const routes: Routes = [
  // Public routes (no auth required)
  {
    path: 'auth/login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(
        (m) => m.LoginComponent
      ),
  },

  // Protected routes (authGuard on parent)
  {
    path: '',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard',
      },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent
          ),
      },
      {
        path: 'usuarios',
        canActivate: [permissionGuard],
        data: { tabla: 'usuario', accion: 'ver_listado' },
        loadChildren: () =>
          import('./features/usuarios/usuarios.routes').then((m) => m.default),
      },
      {
        path: 'leads',
        canActivate: [permissionGuard],
        data: { tabla: 'lead', accion: 'ver_listado' },
        loadComponent: () =>
          import('./shared/components/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
      },
      {
        path: 'personas',
        canActivate: [permissionGuard],
        data: { tabla: 'persona', accion: 'ver_listado' },
        loadComponent: () =>
          import('./shared/components/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
      },
      {
        path: 'empresas',
        canActivate: [permissionGuard],
        data: { tabla: 'empresa', accion: 'ver_listado' },
        loadComponent: () =>
          import('./shared/components/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
      },
      {
        path: 'proveedores',
        canActivate: [permissionGuard],
        data: { tabla: 'proveedor', accion: 'ver_listado' },
        loadComponent: () =>
          import('./shared/components/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
      },
      {
        path: 'servicios',
        canActivate: [permissionGuard],
        data: { tabla: 'servicio', accion: 'ver_listado' },
        loadComponent: () =>
          import('./shared/components/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
      },
      {
        path: 'cotizaciones',
        canActivate: [permissionGuard],
        data: { tabla: 'cotizacion', accion: 'ver_listado' },
        loadComponent: () =>
          import('./shared/components/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
      },
      {
        path: 'eventos',
        canActivate: [permissionGuard],
        data: { tabla: 'evento', accion: 'ver_listado' },
        loadComponent: () =>
          import('./shared/components/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
      },
      {
        path: 'ordenes-compra',
        canActivate: [permissionGuard],
        data: { tabla: 'orden_compra', accion: 'ver_listado' },
        loadComponent: () =>
          import('./shared/components/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
      },
      {
        path: 'mensajes',
        canActivate: [permissionGuard],
        data: { tabla: 'mensaje', accion: 'ver_listado' },
        loadComponent: () =>
          import('./shared/components/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
      },
      {
        path: 'presentaciones',
        canActivate: [permissionGuard],
        data: { tabla: 'presentacion', accion: 'ver_listado' },
        loadComponent: () =>
          import('./shared/components/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
      },
      {
        path: 'inventario',
        canActivate: [permissionGuard],
        data: { tabla: 'insumo', accion: 'ver_listado' },
        loadComponent: () =>
          import('./shared/components/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
      },
      {
        path: 'catalogos',
        canActivate: [permissionGuard],
        data: { tabla: 'catalogo', accion: 'ver_listado' },
        loadComponent: () =>
          import('./shared/components/placeholder/placeholder.component').then(
            (m) => m.PlaceholderComponent
          ),
      },
    ],
  },

  // Wildcard — redirect to dashboard
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];
