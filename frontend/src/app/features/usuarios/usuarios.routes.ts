import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./usuario-list/usuario-list.component').then(
        (m) => m.UsuarioListComponent
      ),
  },
  {
    path: 'nuevo',
    loadComponent: () =>
      import('./usuario-form/usuario-form.component').then(
        (m) => m.UsuarioFormComponent
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./usuario-form/usuario-form.component').then(
        (m) => m.UsuarioFormComponent
      ),
  },
  {
    path: 'roles/:id/permisos',
    loadComponent: () =>
      import('./permiso-editor/permiso-editor.component').then(
        (m) => m.PermisoEditorComponent
      ),
  },
];

export default routes;
