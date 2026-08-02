import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { ApiResponse } from '../../core/models/auth.models';
import { PageResponse } from '../../core/models/pagination.model';
import {
  Cotizacion,
  CambiarEstadoRequest,
  EstadoOption,
  PersonaOption,
  EmpresaOption,
  ServicioOption,
  DescuentoRecargoOption,
} from './cotizacion.models';

@Injectable({ providedIn: 'root' })
export class CotizacionService extends BaseCrudService<Cotizacion> {
  protected baseUrl = '/api/v1/cotizaciones';

  /**
   * Cambia el estado de una cotización.
   */
  cambiarEstado(id: string, request: CambiarEstadoRequest): Observable<Cotizacion> {
    return this.http
      .patch<ApiResponse<Cotizacion>>(`${this.baseUrl}/${id}/estado`, request)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al cambiar estado');
        return response.data;
      }));
  }

  /**
   * Obtiene cotizaciones próximas a vencer.
   */
  getPorVencer(dias: number = 7): Observable<PageResponse<Cotizacion>> {
    const params = new HttpParams().set('dias', String(dias));
    return this.http
      .get<ApiResponse<PageResponse<Cotizacion>>>(`${this.baseUrl}/vencimientos`, { params })
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al obtener vencimientos');
        return response.data;
      }));
  }

  /**
   * Recalcula el total de una cotización.
   */
  recalcularTotal(id: string): Observable<Cotizacion> {
    return this.http
      .post<ApiResponse<Cotizacion>>(`${this.baseUrl}/${id}/recalcular`, {})
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al recalcular total');
        return response.data;
      }));
  }

  /**
   * Descarga el PDF de una cotización.
   */
  downloadPdf(id: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/pdf`, {
      responseType: 'blob',
    });
  }

  /**
   * Obtiene los estados de cotización desde el catálogo.
   */
  getEstados(): Observable<EstadoOption[]> {
    return this.http
      .get<ApiResponse<EstadoOption[]>>('/api/v1/catalogos/estado?contexto=cotizacion')
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  /**
   * Obtiene las personas para asignar a cotización.
   */
  getPersonas(): Observable<PersonaOption[]> {
    return this.http
      .get<ApiResponse<any>>('/api/v1/personas', {
        params: new HttpParams().set('size', '500'),
      })
      .pipe(map((response) => {
        if (!response.success) return [];
        const content = response.data.content ?? response.data;
        return content.map((p: any) => ({ id: p.id, nombre: p.nombre || `${p.nombres} ${p.apellidos}` }));
      }));
  }

  /**
   * Obtiene las empresas para asignar a cotización.
   */
  getEmpresas(): Observable<EmpresaOption[]> {
    return this.http
      .get<ApiResponse<any>>('/api/v1/empresas', {
        params: new HttpParams().set('size', '500'),
      })
      .pipe(map((response) => {
        if (!response.success) return [];
        const content = response.data.content ?? response.data;
        return content.map((e: any) => ({ id: e.id, nombre: e.nombre || e.razonSocial }));
      }));
  }

  /**
   * Obtiene los servicios disponibles para items de cotización.
   */
  getServicios(): Observable<ServicioOption[]> {
    return this.http
      .get<ApiResponse<any>>('/api/v1/servicios', {
        params: new HttpParams().set('size', '500'),
      })
      .pipe(map((response) => {
        if (!response.success) return [];
        const content = response.data.content ?? response.data;
        return content.map((s: any) => ({ id: s.id, nombre: s.nombre }));
      }));
  }

  /**
   * Obtiene los descuentos/recargos disponibles.
   */
  getDescuentosRecargos(): Observable<DescuentoRecargoOption[]> {
    return this.http
      .get<ApiResponse<any>>('/api/v1/descuentos-recargos', {
        params: new HttpParams().set('size', '500'),
      })
      .pipe(map((response) => {
        if (!response.success) return [];
        const content = response.data.content ?? response.data;
        return content.map((d: any) => ({ id: d.id, nombre: d.nombre, valor: d.valor }));
      }));
  }
}
