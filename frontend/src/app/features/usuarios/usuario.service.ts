import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { ApiResponse } from '../../core/models/auth.models';
import { Usuario, Rol } from './usuario.models';

@Injectable({ providedIn: 'root' })
export class UsuarioService extends BaseCrudService<Usuario> {
  protected baseUrl = '/api/v1/usuarios';

  /**
   * Obtiene la lista de roles disponibles.
   */
  getRoles(): Observable<Rol[]> {
    return this.http
      .get<ApiResponse<Rol[]>>(`${this.baseUrl}/roles`)
      .pipe(map((response) => response.success ? response.data : []));
  }

  /**
   * Obtiene la configuración de permisos para un rol.
   */
  getPermisosByRol(rolId: string): Observable<Record<string, Record<string, boolean>>> {
    return this.http
      .get<ApiResponse<Record<string, Record<string, boolean>>>>(`${this.baseUrl}/roles/${rolId}/permisos`)
      .pipe(map((response) => response.success ? response.data : {}));
  }

  /**
   * Actualiza la configuración de permisos para un rol.
   */
  updatePermisosByRol(rolId: string, configuracion: Record<string, Record<string, boolean>>): Observable<any> {
    return this.http
      .put<ApiResponse<any>>(`${this.baseUrl}/roles/${rolId}/permisos`, { configuracion })
      .pipe(map((response) => response.data));
  }
}
