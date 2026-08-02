import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./evento-list/evento-list.component').then(
        (m) => m.EventoListComponent
      ),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./evento-detail/evento-detail.component').then(
        (m) => m.EventoDetailComponent
      ),
  },
];

export default routes;
