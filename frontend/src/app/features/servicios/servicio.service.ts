import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { ApiResponse } from '../../core/models/auth.models';
import { Servicio, CategoriaOption } from './servicio.models';

@Injectable({ providedIn: 'root' })
export class ServicioService extends BaseCrudService<Servicio> {
  protected baseUrl = '/api/v1/servicios';

  /**
   * Obtiene los subservicios (hijos) de un servicio padre.
   */
  getSubservicios(parentId: string): Observable<Servicio[]> {
    return this.http
      .get<ApiResponse<Servicio[]>>(`${this.baseUrl}/${parentId}/subservicios`)
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  /**
   * Asigna una categoría a un servicio.
   */
  categorizar(servicioId: string, categoriaId: string): Observable<Servicio> {
    return this.http
      .put<ApiResponse<Servicio>>(`${this.baseUrl}/${servicioId}/categorizar`, { categoriaId })
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al categorizar servicio');
        return response.data;
      }));
  }

  /**
   * Obtiene las categorías de servicio desde el catálogo.
   */
  getCategorias(): Observable<CategoriaOption[]> {
    return this.http
      .get<ApiResponse<CategoriaOption[]>>('/api/v1/catalogos/categoria-servicio')
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  /**
   * Obtiene todos los servicios (para selección de padre en formulario).
   */
  getAllServicios(): Observable<Servicio[]> {
    return this.http
      .get<ApiResponse<any>>(`${this.baseUrl}`, {
        params: new HttpParams().set('size', '500'),
      })
      .pipe(map((response) => {
        if (!response.success) return [];
        return response.data.content ?? response.data;
      }));
  }
}
