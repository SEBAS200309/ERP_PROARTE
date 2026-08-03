import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { ApiResponse } from '../../core/models/auth.models';
import { CatalogoItem, CreateCatalogoRequest, UpdateCatalogoRequest } from './catalogo.models';

/**
 * Servicio para gestionar catálogos.
 * A diferencia de BaseCrudService, este servicio tiene un baseUrl dinámico
 * basado en el tipo de catálogo seleccionado.
 */
@Injectable({ providedIn: 'root' })
export class CatalogoService {
  private readonly http = inject(HttpClient);
  private readonly apiBase = '/api/v1/catalogos';

  /**
   * Obtiene todos los valores de un tipo de catálogo.
   */
  getAllByTipo(tipo: string, contexto?: string): Observable<CatalogoItem[]> {
    let params = new HttpParams();
    if (contexto) {
      params = params.set('contexto', contexto);
    }
    return this.http
      .get<ApiResponse<CatalogoItem[]>>(`${this.apiBase}/${tipo}`, { params })
      .pipe(map((response) => this.unwrap(response)));
  }

  /**
   * Obtiene un valor de catálogo por ID y tipo.
   */
  getByIdAndTipo(tipo: string, id: string): Observable<CatalogoItem> {
    return this.http
      .get<ApiResponse<CatalogoItem>>(`${this.apiBase}/${tipo}/${id}`)
      .pipe(map((response) => this.unwrap(response)));
  }

  /**
   * Crea un nuevo valor de catálogo.
   */
  createByTipo(tipo: string, dto: CreateCatalogoRequest): Observable<CatalogoItem> {
    return this.http
      .post<ApiResponse<CatalogoItem>>(`${this.apiBase}/${tipo}`, dto)
      .pipe(map((response) => this.unwrap(response)));
  }

  /**
   * Actualiza un valor de catálogo existente.
   */
  updateByTipo(tipo: string, id: string, dto: UpdateCatalogoRequest): Observable<CatalogoItem> {
    return this.http
      .put<ApiResponse<CatalogoItem>>(`${this.apiBase}/${tipo}/${id}`, dto)
      .pipe(map((response) => this.unwrap(response)));
  }

  /**
   * Elimina un valor de catálogo.
   */
  deleteByTipo(tipo: string, id: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.apiBase}/${tipo}/${id}`)
      .pipe(map((response) => this.unwrap(response)));
  }

  /**
   * Desempaqueta ApiResponse: extrae data si success es true,
   * o lanza un error si success es false.
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
}
