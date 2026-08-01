import { inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { ApiResponse } from '../models/auth.models';
import { PageParams, PageResponse } from '../models/pagination.model';

/**
 * Servicio CRUD base genérico.
 * Los servicios de cada feature extienden esta clase para obtener
 * operaciones CRUD estándar con desempaquetado automático de ApiResponse.
 *
 * @example
 * ```typescript
 * @Injectable({ providedIn: 'root' })
 * export class LeadService extends BaseCrudService<Lead> {
 *   protected baseUrl = '/api/v1/leads';
 * }
 * ```
 */
export abstract class BaseCrudService<T> {
  protected abstract baseUrl: string;
  protected readonly http = inject(HttpClient);

  /**
   * Obtiene una lista paginada de recursos.
   */
  getAll(params?: PageParams): Observable<PageResponse<T>> {
    const httpParams = this.buildHttpParams(params);
    return this.http
      .get<ApiResponse<PageResponse<T>>>(this.baseUrl, { params: httpParams })
      .pipe(map((response) => this.unwrap(response)));
  }

  /**
   * Obtiene un recurso por su ID.
   */
  getById(id: string): Observable<T> {
    return this.http
      .get<ApiResponse<T>>(`${this.baseUrl}/${id}`)
      .pipe(map((response) => this.unwrap(response)));
  }

  /**
   * Crea un nuevo recurso.
   */
  create(dto: Partial<T>): Observable<T> {
    return this.http
      .post<ApiResponse<T>>(this.baseUrl, dto)
      .pipe(map((response) => this.unwrap(response)));
  }

  /**
   * Actualiza un recurso existente.
   */
  update(id: string, dto: Partial<T>): Observable<T> {
    return this.http
      .put<ApiResponse<T>>(`${this.baseUrl}/${id}`, dto)
      .pipe(map((response) => this.unwrap(response)));
  }

  /**
   * Elimina un recurso por su ID.
   */
  delete(id: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${id}`)
      .pipe(map((response) => this.unwrap(response)));
  }

  /**
   * Ejecuta una función del servidor asociada al recurso.
   */
  executeFunction(name: string, params: Record<string, any>): Observable<any> {
    return this.http
      .post<ApiResponse<any>>(`${this.baseUrl}/execute/${name}`, { params })
      .pipe(map((response) => this.unwrap(response)));
  }

  /**
   * Desempaqueta ApiResponse: extrae `data` si `success` es true,
   * o lanza un error si `success` es false.
   */
  private unwrap<R>(response: ApiResponse<R>): R {
    if (!response.success) {
      const errorResponse = response as any;
      const message =
        errorResponse.error?.message || response.message || 'Error desconocido del servidor';
      throw new Error(message);
    }
    return response.data;
  }

  /**
   * Construye HttpParams a partir de PageParams,
   * filtrando valores undefined/null.
   */
  private buildHttpParams(params?: PageParams): HttpParams {
    let httpParams = new HttpParams();
    if (!params) return httpParams;

    for (const key of Object.keys(params)) {
      const value = params[key];
      if (value !== undefined && value !== null) {
        httpParams = httpParams.set(key, String(value));
      }
    }
    return httpParams;
  }
}
