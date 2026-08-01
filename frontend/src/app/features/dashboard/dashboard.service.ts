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
   * Usa page=0&size=1 para obtener solo el totalElements de cada recurso.
   *
   * TODO: Cuando el backend tenga un endpoint /api/v1/dashboard/resumen,
   * reemplazar estas múltiples llamadas por una sola.
   */
  getResumen(): Observable<DashboardResumen> {
    const params = new HttpParams().set('page', '0').set('size', '1');

    const leads$ = this.http
      .get<ApiResponse<PageResponse<any>>>('/api/v1/leads', { params })
      .pipe(
        map((res) => (res.success ? res.data.totalElements : 0)),
        catchError(() => of(0))
      );

    const cotizaciones$ = this.http
      .get<ApiResponse<PageResponse<any>>>('/api/v1/cotizaciones', {
        params: params.set('estado', 'PENDIENTE'),
      })
      .pipe(
        map((res) => (res.success ? res.data.totalElements : 0)),
        catchError(() => of(0))
      );

    const eventos$ = this.http
      .get<ApiResponse<PageResponse<any>>>('/api/v1/eventos', { params })
      .pipe(
        map((res) => (res.success ? res.data.totalElements : 0)),
        catchError(() => of(0))
      );

    return forkJoin({ totalLeads: leads$, cotizacionesPendientes: cotizaciones$, eventosProximos: eventos$ });
  }
}
