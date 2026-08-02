import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./orden-compra-list/orden-compra-list.component').then(
        (m) => m.OrdenCompraListComponent
      ),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./orden-compra-form/orden-compra-form.component').then(
        (m) => m.OrdenCompraFormComponent
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./orden-compra-form/orden-compra-form.component').then(
        (m) => m.OrdenCompraFormComponent
      ),
  },
];

export default routes;
