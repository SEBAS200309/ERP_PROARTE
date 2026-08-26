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
        data: { tabla: 'usuarios', accion: 'leer' },
        loadChildren: () =>
          import('./features/usuarios/usuarios.routes').then((m) => m.default),
      },
      {
        path: 'leads',
        canActivate: [permissionGuard],
        data: { tabla: 'leads', accion: 'leer' },
        loadChildren: () =>
          import('./features/leads/leads.routes').then((m) => m.default),
      },
      {
        path: 'personas',
        canActivate: [permissionGuard],
        data: { tabla: 'personas', accion: 'leer' },
        loadChildren: () =>
          import('./features/personas/personas.routes').then((m) => m.default),
      },
      {
        path: 'empresas',
        canActivate: [permissionGuard],
        data: { tabla: 'empresas', accion: 'leer' },
        loadChildren: () =>
          import('./features/empresas/empresas.routes').then((m) => m.default),
      },
      {
        path: 'proveedores',
        canActivate: [permissionGuard],
        data: { tabla: 'proveedores', accion: 'leer' },
        loadChildren: () =>
          import('./features/proveedores/proveedores.routes').then((m) => m.default),
      },
      {
        path: 'servicios',
        canActivate: [permissionGuard],
        data: { tabla: 'servicios', accion: 'leer' },
        loadChildren: () =>
          import('./features/servicios/servicios.routes').then((m) => m.default),
      },
      {
        path: 'descuentos-recargos',
        canActivate: [permissionGuard],
        data: { tabla: 'descuentos-recargos', accion: 'leer' },
        loadChildren: () =>
          import('./features/servicios/servicios.routes').then((m) => m.descuentosRecargosRoutes),
      },
      {
        path: 'cotizaciones',
        canActivate: [permissionGuard],
        data: { tabla: 'cotizaciones', accion: 'leer' },
        loadChildren: () =>
          import('./features/cotizaciones/cotizaciones.routes').then((m) => m.default),
      },
      {
        path: 'eventos',
        canActivate: [permissionGuard],
        data: { tabla: 'eventos', accion: 'leer' },
        loadChildren: () =>
          import('./features/eventos/eventos.routes').then((m) => m.default),
      },
      {
        path: 'ordenes-compra',
        canActivate: [permissionGuard],
        data: { tabla: 'ordenes-compra', accion: 'leer' },
        loadChildren: () =>
          import('./features/ordenes-compra/ordenes-compra.routes').then((m) => m.default),
      },
      {
        path: 'mensajes',
        canActivate: [permissionGuard],
        data: { tabla: 'mensajes', accion: 'leer' },
        loadChildren: () =>
          import('./features/mensajes/mensajes.routes').then((m) => m.default),
      },
      {
        path: 'presentaciones',
        canActivate: [permissionGuard],
        data: { tabla: 'presentaciones', accion: 'leer' },
        loadChildren: () =>
          import('./features/presentaciones/presentaciones.routes').then((m) => m.default),
      },
      {
        path: 'inventario',
        canActivate: [permissionGuard],
        data: { tabla: 'inventario', accion: 'leer' },
        loadChildren: () =>
          import('./features/inventario/inventario.routes').then((m) => m.default),
      },
      {
        path: 'catalogos',
        canActivate: [permissionGuard],
        data: { tabla: 'catalogos', accion: 'leer' },
        loadChildren: () =>
          import('./features/catalogos/catalogos.routes').then((m) => m.default),
      },
    ],
  },

  // Wildcard — redirect to dashboard
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];
