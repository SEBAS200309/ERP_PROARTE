import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { HttpParams } from '@angular/common/http';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { ApiResponse } from '../../core/models/auth.models';
import { Presentacion, ServicioOption } from './presentacion.models';

@Injectable({ providedIn: 'root' })
export class PresentacionService extends BaseCrudService<Presentacion> {
  protected baseUrl = '/api/v1/presentaciones';

  /**
   * Descarga el PDF de una presentación.
   */
  descargarPdf(id: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/pdf`, {
      responseType: 'blob',
    });
  }

  /**
   * Obtiene la lista de servicios disponibles para vincular a una presentación.
   */
  getServicios(): Observable<ServicioOption[]> {
    return this.http
      .get<ApiResponse<any>>('/api/v1/servicios', {
        params: new HttpParams().set('size', '500'),
      })
      .pipe(
        map((response) => {
          if (!response.success) return [];
          const content = response.data.content ?? response.data;
          return content.map((s: any) => ({
            id: s.id,
            nombre: s.nombre || s.titulo || s.id,
          }));
        })
      );
  }
}
