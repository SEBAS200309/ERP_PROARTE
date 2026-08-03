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
        loadChildren: () =>
          import('./features/leads/leads.routes').then((m) => m.default),
      },
      {
        path: 'personas',
        canActivate: [permissionGuard],
        data: { tabla: 'persona', accion: 'ver_listado' },
        loadChildren: () =>
          import('./features/personas/personas.routes').then((m) => m.default),
      },
      {
        path: 'empresas',
        canActivate: [permissionGuard],
        data: { tabla: 'empresa', accion: 'ver_listado' },
        loadChildren: () =>
          import('./features/empresas/empresas.routes').then((m) => m.default),
      },
      {
        path: 'proveedores',
        canActivate: [permissionGuard],
        data: { tabla: 'proveedor', accion: 'ver_listado' },
        loadChildren: () =>
          import('./features/proveedores/proveedores.routes').then((m) => m.default),
      },
      {
        path: 'servicios',
        canActivate: [permissionGuard],
        data: { tabla: 'servicio', accion: 'ver_listado' },
        loadChildren: () =>
          import('./features/servicios/servicios.routes').then((m) => m.default),
      },
      {
        path: 'descuentos-recargos',
        canActivate: [permissionGuard],
        data: { tabla: 'descuentos_recargos', accion: 'ver_listado' },
        loadChildren: () =>
          import('./features/servicios/servicios.routes').then((m) => m.descuentosRecargosRoutes),
      },
      {
        path: 'cotizaciones',
        canActivate: [permissionGuard],
        data: { tabla: 'cotizacion', accion: 'ver_listado' },
        loadChildren: () =>
          import('./features/cotizaciones/cotizaciones.routes').then((m) => m.default),
      },
      {
        path: 'eventos',
        canActivate: [permissionGuard],
        data: { tabla: 'evento', accion: 'ver_listado' },
        loadChildren: () =>
          import('./features/eventos/eventos.routes').then((m) => m.default),
      },
      {
        path: 'ordenes-compra',
        canActivate: [permissionGuard],
        data: { tabla: 'orden_compra', accion: 'ver_listado' },
        loadChildren: () =>
          import('./features/ordenes-compra/ordenes-compra.routes').then((m) => m.default),
      },
      {
        path: 'mensajes',
        canActivate: [permissionGuard],
        data: { tabla: 'mensaje', accion: 'ver_listado' },
        loadChildren: () =>
          import('./features/mensajes/mensajes.routes').then((m) => m.default),
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
