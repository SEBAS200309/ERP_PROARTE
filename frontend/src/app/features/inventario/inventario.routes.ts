import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./inventario-stock/inventario-stock.component').then(
        (m) => m.InventarioStockComponent
      ),
  },
  {
    path: 'ingresos',
    loadComponent: () =>
      import('./ingreso-list/ingreso-list.component').then(
        (m) => m.IngresoListComponent
      ),
  },
  {
    path: 'ingresos/nuevo',
    loadComponent: () =>
      import('./ingreso-form/ingreso-form.component').then(
        (m) => m.IngresoFormComponent
      ),
  },
  {
    path: 'retiros',
    loadComponent: () =>
      import('./retiro-list/retiro-list.component').then(
        (m) => m.RetiroListComponent
      ),
  },
  {
    path: 'retiros/nuevo',
    loadComponent: () =>
      import('./retiro-form/retiro-form.component').then(
        (m) => m.RetiroFormComponent
      ),
  },
];

export default routes;
