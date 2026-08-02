import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./servicio-list/servicio-list.component').then(
        (m) => m.ServicioListComponent
      ),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./servicio-form/servicio-form.component').then(
        (m) => m.ServicioFormComponent
      ),
  },
  {
    path: 'arbol',
    loadComponent: () =>
      import('./subservicio-tree/subservicio-tree.component').then(
        (m) => m.SubservicioTreeComponent
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./servicio-form/servicio-form.component').then(
        (m) => m.ServicioFormComponent
      ),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./servicio-form/servicio-form.component').then(
        (m) => m.ServicioFormComponent
      ),
  },
];

export const descuentosRecargosRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./porcentaje-list/porcentaje-list.component').then(
        (m) => m.PorcentajeListComponent
      ),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./porcentaje-form/porcentaje-form.component').then(
        (m) => m.PorcentajeFormComponent
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./porcentaje-form/porcentaje-form.component').then(
        (m) => m.PorcentajeFormComponent
      ),
  },
];

export default routes;
