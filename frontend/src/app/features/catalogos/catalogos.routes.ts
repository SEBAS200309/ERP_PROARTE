import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./catalogo-list/catalogo-list.component').then(
        (m) => m.CatalogoListComponent
      ),
  },
  {
    path: ':tipo',
    loadComponent: () =>
      import('./catalogo-list/catalogo-list.component').then(
        (m) => m.CatalogoListComponent
      ),
  },
  {
    path: ':tipo/nuevo',
    loadComponent: () =>
      import('./catalogo-form/catalogo-form.component').then(
        (m) => m.CatalogoFormComponent
      ),
  },
  {
    path: ':tipo/:id/editar',
    loadComponent: () =>
      import('./catalogo-form/catalogo-form.component').then(
        (m) => m.CatalogoFormComponent
      ),
  },
];

export default routes;
