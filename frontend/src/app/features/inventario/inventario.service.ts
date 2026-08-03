import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { HttpParams } from '@angular/common/http';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { ApiResponse } from '../../core/models/auth.models';
import { PageParams, PageResponse } from '../../core/models/pagination.model';
import { Insumo, Movimiento, CreateMovimientoRequest } from './inventario.models';

@Injectable({ providedIn: 'root' })
export class InventarioService extends BaseCrudService<Insumo> {
  protected baseUrl = '/api/v1/inventario';

  /**
   * Registra un ingreso de stock.
   */
  registrarIngreso(request: CreateMovimientoRequest): Observable<Movimiento> {
    return this.http
      .post<ApiResponse<Movimiento>>(`${this.baseUrl}/ingresos`, request)
      .pipe(map((response) => this.unwrapResponse(response)));
  }

  /**
   * Registra un retiro de stock.
   */
  registrarRetiro(request: CreateMovimientoRequest): Observable<Movimiento> {
    return this.http
      .post<ApiResponse<Movimiento>>(`${this.baseUrl}/retiros`, request)
      .pipe(map((response) => this.unwrapResponse(response)));
  }

  /**
   * Obtiene los movimientos de un insumo con filtro por tipo.
   */
  getMovimientos(insumoId: string, tipo?: string, params?: PageParams): Observable<PageResponse<Movimiento>> {
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
      .get<ApiResponse<PageResponse<Movimiento>>>(`${this.baseUrl}/${insumoId}/movimientos`, { params: httpParams })
      .pipe(map((response) => this.unwrapResponse(response)));
  }

  /**
   * Obtiene todos los insumos sin paginación (para dropdowns).
   */
  getAllInsumos(): Observable<Insumo[]> {
    const httpParams = new HttpParams().set('size', '500');
    return this.http
      .get<ApiResponse<PageResponse<Insumo>>>(this.baseUrl, { params: httpParams })
      .pipe(
        map((response) => {
          if (!response.success) return [];
          return response.data.content ?? [];
        })
      );
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
