import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./mensaje-list/mensaje-list.component').then(
        (m) => m.MensajeListComponent
      ),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./mensaje-form/mensaje-form.component').then(
        (m) => m.MensajeFormComponent
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./mensaje-form/mensaje-form.component').then(
        (m) => m.MensajeFormComponent
      ),
  },
];

export default routes;
