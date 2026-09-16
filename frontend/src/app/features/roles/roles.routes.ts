import { Routes } from '@angular/router';

const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./rol-list/rol-list.component').then((m) => m.RolListComponent),
    },
    {
        path: 'nuevo',
        loadComponent: () =>
            import('./rol-form/rol-form.component').then((m) => m.RolFormComponent),
    },
    {
        path: ':id/editar',
        loadComponent: () =>
            import('./rol-form/rol-form.component').then((m) => m.RolFormComponent),
    },
    {
        path: ':id',
        loadComponent: () =>
            import('./rol-detail/rol-detail.component').then((m) => m.RolDetailComponent),
    },
];

export default routes;