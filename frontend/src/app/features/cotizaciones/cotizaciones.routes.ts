import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./cotizacion-list/cotizacion-list.component').then(
        (m) => m.CotizacionListComponent
      ),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./cotizacion-form/cotizacion-form.component').then(
        (m) => m.CotizacionFormComponent
      ),
  },
  {
    path: 'vencimientos',
    loadComponent: () =>
      import('./cotizacion-vencimiento/cotizacion-vencimiento.component').then(
        (m) => m.CotizacionVencimientoComponent
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./cotizacion-form/cotizacion-form.component').then(
        (m) => m.CotizacionFormComponent
      ),
  },
];

export default routes;
