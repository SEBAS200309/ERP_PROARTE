import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./proveedor-list/proveedor-list.component').then(
        (m) => m.ProveedorListComponent
      ),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./proveedor-form/proveedor-form.component').then(
        (m) => m.ProveedorFormComponent
      ),
  },
  {
    path: 'solicitudes',
    loadComponent: () =>
      import('./solicitud-list/solicitud-list.component').then(
        (m) => m.SolicitudListComponent
      ),
  },
  {
    path: 'solicitudes/nuevo',
    loadComponent: () =>
      import('./solicitud-form/solicitud-form.component').then(
        (m) => m.SolicitudFormComponent
      ),
  },
  {
    path: 'solicitudes/:id/editar',
    loadComponent: () =>
      import('./solicitud-form/solicitud-form.component').then(
        (m) => m.SolicitudFormComponent
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./proveedor-form/proveedor-form.component').then(
        (m) => m.ProveedorFormComponent
      ),
  },
  {
    path: ':id/portafolio',
    loadComponent: () =>
      import('./portafolio/portafolio.component').then(
        (m) => m.PortafolioComponent
      ),
  },
];

export default routes;
