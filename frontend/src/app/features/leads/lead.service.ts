import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { ApiResponse } from '../../core/models/auth.models';
import { PageResponse } from '../../core/models/pagination.model';
import { Lead, EstadoCatalogo, PersonaOption, EmpresaOption } from './lead.models';

@Injectable({ providedIn: 'root' })
export class LeadService extends BaseCrudService<Lead> {
  protected baseUrl = '/api/v1/leads';

  /**
   * Obtiene estadísticas de leads agrupadas por estado.
   */
  getEstadisticas(): Observable<Record<string, number>> {
    return this.http
      .get<ApiResponse<{ estadisticas: Record<string, number> }>>(`${this.baseUrl}/estadisticas`)
      .pipe(map((response) => response.success ? response.data.estadisticas : {}));
  }

  /**
   * Obtiene los estados disponibles para leads desde catálogos.
   */
  getEstados(): Observable<EstadoCatalogo[]> {
    return this.http
      .get<ApiResponse<EstadoCatalogo[]>>('/api/v1/catalogos/estado', {
        params: new HttpParams().set('contexto', 'lead'),
      })
      .pipe(map((response) => response.success ? response.data : []));
  }

  /**
   * Obtiene las personas disponibles para asociar al lead.
   */
  getPersonas(): Observable<PersonaOption[]> {
    return this.http
      .get<ApiResponse<PageResponse<PersonaOption>>>('/api/v1/personas', {
        params: new HttpParams().set('size', '200'),
      })
      .pipe(map((response) => response.success ? response.data.content : []));
  }

  /**
   * Obtiene las empresas disponibles para asociar al lead.
   */
  getEmpresas(): Observable<EmpresaOption[]> {
    return this.http
      .get<ApiResponse<PageResponse<EmpresaOption>>>('/api/v1/empresas', {
        params: new HttpParams().set('size', '200'),
      })
      .pipe(map((response) => response.success ? response.data.content : []));
  }
}
