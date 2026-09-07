import { Routes } from '@angular/router';

const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./proveedor-persona-list/proveedor-persona-list.component').then((m) => m.ProveedorPersonaListComponent),
    },
    {
        path: 'nuevo',
        loadComponent: () => import('./proveedor-persona-form/proveedor-persona-form.component').then((m) => m.ProveedorPersonaFormComponent),
    },
    {
        path: 'solicitudes',
        loadComponent: () => import('./solicitud-list/solicitud-list.component').then((m) => m.SolicitudListComponent),
    },
    {
        path: 'solicitudes/nuevo',
        loadComponent: () => import('./solicitud-form/solicitud-form.component').then((m) => m.SolicitudFormComponent),
    },
    {
        path: 'solicitudes/:id/editar',
        loadComponent: () => import('./solicitud-form/solicitud-form.component').then((m) => m.SolicitudFormComponent),
    },
    {
        path: ':id/editar',
        loadComponent: () => import('./proveedor-persona-form/proveedor-persona-form.component').then((m) => m.ProveedorPersonaFormComponent),
    },
    {
        path: ':id/portafolio',
        loadComponent: () => import('./portafolio/portafolio.component').then((m) => m.PortafolioComponent),
    }
];
export default routes