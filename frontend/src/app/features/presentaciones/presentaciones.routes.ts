import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./presentacion-list/presentacion-list.component').then(
        (m) => m.PresentacionListComponent
      ),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./presentacion-form/presentacion-form.component').then(
        (m) => m.PresentacionFormComponent
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./presentacion-form/presentacion-form.component').then(
        (m) => m.PresentacionFormComponent
      ),
  },
];

export default routes;
