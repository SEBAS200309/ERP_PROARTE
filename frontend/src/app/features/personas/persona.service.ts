import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { ApiResponse } from '../../core/models/auth.models';
import { PageResponse } from '../../core/models/pagination.model';
import { Persona, CatalogoOption, AsociarEmpresaRequest } from './persona.models';

@Injectable({ providedIn: 'root' })
export class PersonaService extends BaseCrudService<Persona> {
  protected baseUrl = '/api/v1/personas';

  /**
   * Obtiene los tipos de documento disponibles desde catálogos.
   */
  getTiposDocumento(): Observable<CatalogoOption[]> {
    return this.http
      .get<ApiResponse<CatalogoOption[]>>('/api/v1/catalogos/tipo-documento')
      .pipe(map((response) => response.success ? response.data : []));
  }

  /**
   * Obtiene los roles de entidad disponibles desde catálogos.
   */
  getRolesEntidad(): Observable<CatalogoOption[]> {
    return this.http
      .get<ApiResponse<CatalogoOption[]>>('/api/v1/catalogos/rol-entidad')
      .pipe(map((response) => response.success ? response.data : []));
  }

  /**
   * Asocia una persona a una empresa.
   */
  asociarEmpresa(personaId: string, request: AsociarEmpresaRequest): Observable<any> {
    return this.http
      .post<ApiResponse<any>>(`${this.baseUrl}/${personaId}/asociar-empresa`, request)
      .pipe(map((response) => response.success ? response.data : null));
  }

  /**
   * Asigna un rol de entidad a una persona.
   */
  asignarRol(personaId: string, rolEntidadId: string): Observable<Persona> {
    return this.http
      .put<ApiResponse<Persona>>(`${this.baseUrl}/${personaId}/asignar-rol`, { rolEntidadId })
      .pipe(map((response) => {
        if (!response.success) {
          throw new Error(response.message || 'Error al asignar rol');
        }
        return response.data;
      }));
  }

  /**
   * Obtiene los leads asociados a una persona.
   */
  getLeads(personaId: string): Observable<any[]> {
    return this.http
      .get<ApiResponse<PageResponse<any>>>('/api/v1/leads', {
        params: new HttpParams().set('personaId', personaId).set('size', '100'),
      })
      .pipe(map((response) => response.success ? response.data.content : []));
  }

  /**
   * Obtiene las cotizaciones asociadas a una persona.
   */
  getCotizaciones(personaId: string): Observable<any[]> {
    return this.http
      .get<ApiResponse<PageResponse<any>>>('/api/v1/cotizaciones', {
        params: new HttpParams().set('personaId', personaId).set('size', '100'),
      })
      .pipe(map((response) => response.success ? response.data.content : []));
  }
}
