import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { ApiResponse } from '../../core/models/auth.models';
import { DescuentoRecargo, AplicarDescuentoRequest, CategoriaOption } from './servicio.models';

@Injectable({ providedIn: 'root' })
export class PorcentajeService extends BaseCrudService<DescuentoRecargo> {
  protected baseUrl = '/api/v1/descuentos-recargos';

  /**
   * Aplica un descuento o recargo a un servicio, persona o empresa.
   */
  aplicar(dto: AplicarDescuentoRequest): Observable<any> {
    return this.http
      .post<ApiResponse<any>>(`${this.baseUrl}/aplicar`, dto)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al aplicar descuento/recargo');
        return response.data;
      }));
  }

  /**
   * Obtiene los tipos de descuento/recargo desde el catálogo.
   */
  getTipos(): Observable<CategoriaOption[]> {
    return this.http
      .get<ApiResponse<CategoriaOption[]>>('/api/v1/catalogos/tipo-descuento-recargo')
      .pipe(map((response) => (response.success ? response.data : [])));
  }
}
