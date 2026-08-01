import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { ApiResponse } from '../../core/models/auth.models';
import { PageResponse } from '../../core/models/pagination.model';
import { Empresa, CatalogoOption, PersonaAsociada } from './empresa.models';

@Injectable({ providedIn: 'root' })
export class EmpresaService extends BaseCrudService<Empresa> {
  protected baseUrl = '/api/v1/empresas';

  /**
   * Obtiene los roles de entidad disponibles desde catálogos.
   */
  getRolesEntidad(): Observable<CatalogoOption[]> {
    return this.http
      .get<ApiResponse<CatalogoOption[]>>('/api/v1/catalogos/rol-entidad')
      .pipe(map((response) => response.success ? response.data : []));
  }

  /**
   * Asigna un rol de entidad a una empresa.
   */
  asignarRol(empresaId: string, rolEntidadId: string): Observable<Empresa> {
    return this.http
      .put<ApiResponse<Empresa>>(`${this.baseUrl}/${empresaId}/asignar-rol`, { rolEntidadId })
      .pipe(map((response) => {
        if (!response.success) {
          throw new Error(response.message || 'Error al asignar rol');
        }
        return response.data;
      }));
  }

  /**
   * Obtiene las personas asociadas a una empresa.
   */
  getPersonasAsociadas(empresaId: string): Observable<PersonaAsociada[]> {
    return this.http
      .get<ApiResponse<PageResponse<PersonaAsociada>>>('/api/v1/personas', {
        params: new HttpParams().set('empresaId', empresaId).set('size', '100'),
      })
      .pipe(map((response) => response.success ? response.data.content : []));
  }
}
