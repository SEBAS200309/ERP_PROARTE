import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./lead-list/lead-list.component').then(
        (m) => m.LeadListComponent
      ),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./lead-form/lead-form.component').then(
        (m) => m.LeadFormComponent
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./lead-form/lead-form.component').then(
        (m) => m.LeadFormComponent
      ),
  },
];

export default routes;
