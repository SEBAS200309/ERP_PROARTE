import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { ApiResponse } from '../../core/models/auth.models';
import { PageParams, PageResponse } from '../../core/models/pagination.model';
import {
  Proveedor,
  PortafolioItem,
  CreatePortafolioRequest,
  UpdatePortafolioRequest,
  SolicitudServicio,
  CreateSolicitudRequest,
  UpdateSolicitudRequest,
  PersonaOption,
  EmpresaOption,
  ServicioOption,
  CatalogoOption,
  EventoOption,
} from './proveedor.models';

@Injectable({ providedIn: 'root' })
export class ProveedorService extends BaseCrudService<Proveedor> {
  protected baseUrl = '/api/v1/proveedores';

  /** Obtiene proveedores vinculados a personas (rol proveedor en personas naturales) */
  getAllPersonas(params?: PageParams): Observable<PageResponse<Proveedor>> {
    return this.getAll({ ...params, tipo: 'persona' });
  }

  /** Obtiene proveedores vinculados a empresas */
  getAllEmpresas(params?: PageParams): Observable<PageResponse<Proveedor>> {
    return this.getAll({ ...params, tipo: 'empresa' });
  }

  // ===================== PORTAFOLIO =====================

  getPortafolio(proveedorId: string): Observable<PortafolioItem[]> {
    return this.http
      .get<ApiResponse<PortafolioItem[]>>(`${this.baseUrl}/${proveedorId}/portafolio`)
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  createPortafolio(proveedorId: string, dto: CreatePortafolioRequest): Observable<PortafolioItem> {
    return this.http
      .post<ApiResponse<PortafolioItem>>(`${this.baseUrl}/${proveedorId}/portafolio`, dto)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al crear portafolio');
        return response.data;
      }));
  }

  updatePortafolio(portafolioId: string, dto: UpdatePortafolioRequest): Observable<PortafolioItem> {
    return this.http
      .put<ApiResponse<PortafolioItem>>(`${this.baseUrl}/portafolio/${portafolioId}`, dto)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al actualizar portafolio');
        return response.data;
      }));
  }

  deletePortafolio(portafolioId: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/portafolio/${portafolioId}`)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al eliminar portafolio');
      }));
  }

  // ===================== SOLICITUDES =====================

  getSolicitudes(params?: PageParams): Observable<PageResponse<SolicitudServicio>> {
    let httpParams = new HttpParams();
    if (params) {
      for (const key of Object.keys(params)) {
        const value = params[key];
        if (value !== undefined && value !== null) {
          httpParams = httpParams.set(key, String(value));
        }
      }
    }
    return this.http
      .get<ApiResponse<PageResponse<SolicitudServicio>>>(`${this.baseUrl}/solicitudes`, { params: httpParams })
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al cargar solicitudes');
        return response.data;
      }));
  }

  getSolicitudesByProveedor(proveedorId: string, params?: PageParams): Observable<PageResponse<SolicitudServicio>> {
    let httpParams = new HttpParams();
    if (params) {
      for (const key of Object.keys(params)) {
        const value = params[key];
        if (value !== undefined && value !== null) {
          httpParams = httpParams.set(key, String(value));
        }
      }
    }
    return this.http
      .get<ApiResponse<PageResponse<SolicitudServicio>>>(`${this.baseUrl}/${proveedorId}/solicitudes`, { params: httpParams })
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al cargar solicitudes');
        return response.data;
      }));
  }

  createSolicitud(dto: CreateSolicitudRequest): Observable<SolicitudServicio> {
    return this.http
      .post<ApiResponse<SolicitudServicio>>(`${this.baseUrl}/solicitudes`, dto)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al crear solicitud');
        return response.data;
      }));
  }

  updateSolicitud(solicitudId: string, dto: UpdateSolicitudRequest): Observable<SolicitudServicio> {
    return this.http
      .put<ApiResponse<SolicitudServicio>>(`${this.baseUrl}/solicitudes/${solicitudId}`, dto)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al actualizar solicitud');
        return response.data;
      }));
  }

  deleteSolicitud(solicitudId: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/solicitudes/${solicitudId}`)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al eliminar solicitud');
      }));
  }

  // ===================== CATÁLOGOS =====================

  getPersonas(): Observable<PersonaOption[]> {
    return this.http
      .get<ApiResponse<PageResponse<PersonaOption>>>('/api/v1/personas', {
        params: new HttpParams().set('size', '200'),
      })
      .pipe(map((response) => (response.success ? response.data.content : [])));
  }

  getEmpresas(): Observable<EmpresaOption[]> {
    return this.http
      .get<ApiResponse<PageResponse<EmpresaOption>>>('/api/v1/empresas', {
        params: new HttpParams().set('size', '200'),
      })
      .pipe(map((response) => (response.success ? response.data.content : [])));
  }

  getServicios(): Observable<ServicioOption[]> {
    return this.http
      .get<ApiResponse<ServicioOption[]>>('/api/v1/servicios')
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  getEstados(contexto: string): Observable<CatalogoOption[]> {
    return this.http
      .get<ApiResponse<CatalogoOption[]>>('/api/v1/catalogos/estado', {
        params: new HttpParams().set('contexto', contexto),
      })
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  getEventos(): Observable<EventoOption[]> {
    return this.http
      .get<ApiResponse<PageResponse<EventoOption>>>('/api/v1/eventos', {
        params: new HttpParams().set('size', '200'),
      })
      .pipe(map((response) => (response.success ? response.data.content : [])));
  }
}
