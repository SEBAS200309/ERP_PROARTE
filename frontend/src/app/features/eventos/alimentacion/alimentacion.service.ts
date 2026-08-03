import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { ApiResponse } from '../../../core/models/auth.models';
import { PageParams, PageResponse } from '../../../core/models/pagination.model';
import { Alimentacion, CreateAlimentacionRequest } from './alimentacion.models';

@Injectable({ providedIn: 'root' })
export class AlimentacionService {
  private readonly http = inject(HttpClient);

  private buildUrl(eventoId: string): string {
    return `/api/v1/eventos/${eventoId}/alimentacion`;
  }

  /**
   * Obtiene los movimientos de alimentación de un evento, con filtro opcional por tipo.
   */
  getByEvento(eventoId: string, tipo?: string, params?: PageParams): Observable<PageResponse<Alimentacion>> {
    let httpParams = new HttpParams();
    if (tipo) {
      httpParams = httpParams.set('tipo', tipo);
    }
    if (params) {
      if (params.page !== undefined && params.page !== null) {
        httpParams = httpParams.set('page', String(params.page));
      }
      if (params.size !== undefined && params.size !== null) {
        httpParams = httpParams.set('size', String(params.size));
      }
      if (params.sort) {
        httpParams = httpParams.set('sort', params.sort);
      }
    }
    return this.http
      .get<ApiResponse<PageResponse<Alimentacion>>>(this.buildUrl(eventoId), { params: httpParams })
      .pipe(map((response) => this.unwrapResponse(response)));
  }

  /**
   * Registra un ingreso de alimentación para el evento.
   */
  registrarIngreso(eventoId: string, request: CreateAlimentacionRequest): Observable<Alimentacion> {
    return this.http
      .post<ApiResponse<Alimentacion>>(`${this.buildUrl(eventoId)}/ingresos`, request)
      .pipe(map((response) => this.unwrapResponse(response)));
  }

  /**
   * Registra un retiro de alimentación para el evento.
   */
  registrarRetiro(eventoId: string, request: CreateAlimentacionRequest): Observable<Alimentacion> {
    return this.http
      .post<ApiResponse<Alimentacion>>(`${this.buildUrl(eventoId)}/retiros`, request)
      .pipe(map((response) => this.unwrapResponse(response)));
  }

  private unwrapResponse<R>(response: ApiResponse<R>): R {
    if (!response.success) {
      const errorResponse = response as any;
      const message = errorResponse.error?.message || response.message || 'Error desconocido del servidor';
      const error: any = new Error(message);
      error.code = errorResponse.error?.code;
      throw error;
    }
    return response.data;
  }
}
