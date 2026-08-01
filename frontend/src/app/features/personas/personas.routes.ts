import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./persona-list/persona-list.component').then(
        (m) => m.PersonaListComponent
      ),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./persona-form/persona-form.component').then(
        (m) => m.PersonaFormComponent
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./persona-form/persona-form.component').then(
        (m) => m.PersonaFormComponent
      ),
  },
];

export default routes;
