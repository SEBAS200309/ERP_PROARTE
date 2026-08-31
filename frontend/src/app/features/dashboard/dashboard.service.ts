import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, forkJoin, map, catchError, of } from 'rxjs';

import { ApiResponse } from '../../core/models/auth.models';
import { PageResponse } from '../../core/models/pagination.model';

export interface DashboardResumen {
  totalLeads: number;
  cotizacionesPendientes: number;
  eventosProximos: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  /**
   * Obtiene el resumen del dashboard consultando los endpoints existentes.
   * Utiliza el endpoint optimizado de conteo para leads y paginación mínima para los demás.
   */
  getResumen(): Observable<DashboardResumen> {
    const params = new HttpParams().set('page', '0').set('size', '1');

    // Consulta directa y optimizada que retorna únicamente el número entero
    const leads$ = this.http
      .get<ApiResponse<number>>('/api/v1/leads/count')
      .pipe(
        map((res) => (res.success ? res.data : 0)),
        catchError(() => of(0))
      );

    // Consulta de cuenta de cotizacion en estado Activo que no tienen un evento asignado
    const cotizaciones$ = this.http
      .get<ApiResponse<number>>('/api/v1/cotizaciones/count')
      .pipe(
        map((res) => (res.success ? res.data : 0)),
        catchError(() => of(0))
      );

    const eventos$ = this.http
      .get<ApiResponse<PageResponse<any>>>('/api/v1/eventos', { params })
      .pipe(
        map((res) => (res.success ? res.data.totalElements : 0)),
        catchError(() => of(0))
      );

    return forkJoin({
      totalLeads: leads$,
      cotizacionesPendientes: cotizaciones$,
      eventosProximos: eventos$
    });
  }
}