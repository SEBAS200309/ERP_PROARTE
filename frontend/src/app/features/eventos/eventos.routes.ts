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
  {
    path: ':id/personal',
    loadComponent: () =>
      import('./evento-personal/personal-list/personal-list.component').then(
        (m) => m.PersonalListComponent
      ),
  },
];

export default routes;
