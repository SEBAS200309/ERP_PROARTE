import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { ApiResponse } from '../../core/models/auth.models';
import { Usuario, Rol } from './usuario.models';

@Injectable({ providedIn: 'root' })
export class UsuarioService extends BaseCrudService<Usuario> {
  protected baseUrl = '/api/v1/usuarios';
  private rolesUrl = '/api/v1/roles';

  /**
   * Obtiene la lista de roles disponibles.
   */
  getRoles(): Observable<Rol[]> {
    return this.http
      .get<ApiResponse<Rol[]>>(this.rolesUrl)
      .pipe(map((response) => (response.success ? response.data : [])));
  }
}