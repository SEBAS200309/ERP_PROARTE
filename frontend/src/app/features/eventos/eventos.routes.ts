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
  {
    path: ':id/alimentacion',
    loadComponent: () =>
      import('./alimentacion/alimentacion-list/alimentacion-list.component').then(
        (m) => m.AlimentacionListComponent
      ),
  },
  {
    path: ':id/alimentacion/ingresos/nuevo',
    loadComponent: () =>
      import('./alimentacion/alimentacion-ingreso-form/alimentacion-ingreso-form.component').then(
        (m) => m.AlimentacionIngresoFormComponent
      ),
  },
  {
    path: ':id/alimentacion/retiros/nuevo',
    loadComponent: () =>
      import('./alimentacion/alimentacion-retiro-form/alimentacion-retiro-form.component').then(
        (m) => m.AlimentacionRetiroFormComponent
      ),
  },
];

export default routes;
