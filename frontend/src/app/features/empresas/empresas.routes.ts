import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./empresa-list/empresa-list.component').then(
        (m) => m.EmpresaListComponent
      ),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./empresa-form/empresa-form.component').then(
        (m) => m.EmpresaFormComponent
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./empresa-form/empresa-form.component').then(
        (m) => m.EmpresaFormComponent
      ),
  },
];

export default routes;
