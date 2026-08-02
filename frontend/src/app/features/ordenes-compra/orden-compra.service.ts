import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { ApiResponse } from '../../core/models/auth.models';
import { OrdenCompra, EstadoOption, SolicitudOption } from './orden-compra.models';

@Injectable({ providedIn: 'root' })
export class OrdenCompraService extends BaseCrudService<OrdenCompra> {
  protected baseUrl = '/api/v1/ordenes-compra';

  /**
   * Descarga el archivo Excel de órdenes de compra.
   * Puede filtrar por estadoId o por una lista de IDs específicos.
   */
  descargarExcel(estadoId?: string, ids?: string[]): Observable<Blob> {
    let params = new HttpParams();
    if (estadoId) {
      params = params.set('estadoId', estadoId);
    }
    if (ids && ids.length > 0) {
      ids.forEach((id) => {
        params = params.append('ids', id);
      });
    }
    return this.http.get(`${this.baseUrl}/descargar-excel`, {
      params,
      responseType: 'blob',
    });
  }

  /**
   * Obtiene los estados disponibles para órdenes de compra desde el catálogo.
   */
  getEstados(): Observable<EstadoOption[]> {
    return this.http
      .get<ApiResponse<EstadoOption[]>>('/api/v1/catalogos/estado?contexto=orden')
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  /**
   * Obtiene las solicitudes de servicio disponibles para vincular.
   */
  getSolicitudes(): Observable<SolicitudOption[]> {
    return this.http
      .get<ApiResponse<any>>('/api/v1/proveedores/solicitudes', {
        params: new HttpParams().set('size', '500'),
      })
      .pipe(
        map((response) => {
          if (!response.success) return [];
          const content = response.data.content ?? response.data;
          return content.map((s: any) => ({
            id: s.id,
            nombre: s.codigo || s.nombre || s.id,
          }));
        })
      );
  }
}
