import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { BaseCrudService } from '../../core/services/base-crud.service';
import { ApiResponse } from '../../core/models/auth.models';
import {
  Evento,
  EventoContacto,
  EventoContactoRequest,
  EventoProveedor,
  EventoProveedorRequest,
  Observacion,
  ObservacionRequest,
  EventoInsumo,
  EventoInsumoRequest,
  EstadoOption,
  RolEventoOption,
  PersonaOption,
  ProveedorOption,
  ServicioOption,
  InsumoOption,
} from './evento.models';

@Injectable({ providedIn: 'root' })
export class EventoService extends BaseCrudService<Evento> {
  protected baseUrl = '/api/v1/eventos';

  // ===================== CREAR DESDE COTIZACIÓN =====================

  crearDesdeCotizacion(cotizacionId: string): Observable<Evento> {
    return this.http
      .post<ApiResponse<Evento>>(`${this.baseUrl}/crear-desde-cotizacion`, { cotizacionId })
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al crear evento desde cotización');
        return response.data;
      }));
  }

  // ===================== CONTACTOS / PERSONAS =====================

  getContactos(eventoId: string): Observable<EventoContacto[]> {
    return this.http
      .get<ApiResponse<EventoContacto[]>>(`${this.baseUrl}/${eventoId}/personas`)
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  addContacto(eventoId: string, request: EventoContactoRequest): Observable<EventoContacto> {
    return this.http
      .post<ApiResponse<EventoContacto>>(`${this.baseUrl}/${eventoId}/personas`, request)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al agregar contacto');
        return response.data;
      }));
  }

  removeContacto(eventoId: string, contactoId: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${eventoId}/personas/${contactoId}`)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al eliminar contacto');
      }));
  }

  // ===================== PROVEEDORES =====================

  getProveedores(eventoId: string): Observable<EventoProveedor[]> {
    return this.http
      .get<ApiResponse<EventoProveedor[]>>(`${this.baseUrl}/${eventoId}/proveedores`)
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  addProveedor(eventoId: string, request: EventoProveedorRequest): Observable<EventoProveedor> {
    return this.http
      .post<ApiResponse<EventoProveedor>>(`${this.baseUrl}/${eventoId}/proveedores`, request)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al agregar proveedor');
        return response.data;
      }));
  }

  removeProveedor(eventoId: string, proveedorId: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${eventoId}/proveedores/${proveedorId}`)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al eliminar proveedor');
      }));
  }

  // ===================== OBSERVACIONES =====================

  getObservaciones(eventoId: string): Observable<Observacion[]> {
    return this.http
      .get<ApiResponse<Observacion[]>>(`${this.baseUrl}/${eventoId}/observaciones`)
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  addObservacion(eventoId: string, request: ObservacionRequest): Observable<Observacion> {
    return this.http
      .post<ApiResponse<Observacion>>(`${this.baseUrl}/${eventoId}/observaciones`, request)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al agregar observación');
        return response.data;
      }));
  }

  updateObservacion(eventoId: string, observacionId: string, request: ObservacionRequest): Observable<Observacion> {
    return this.http
      .put<ApiResponse<Observacion>>(`${this.baseUrl}/${eventoId}/observaciones/${observacionId}`, request)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al actualizar observación');
        return response.data;
      }));
  }

  // ===================== INSUMOS =====================

  getInsumos(eventoId: string): Observable<EventoInsumo[]> {
    return this.http
      .get<ApiResponse<EventoInsumo[]>>(`${this.baseUrl}/${eventoId}/insumos`)
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  addInsumo(eventoId: string, request: EventoInsumoRequest): Observable<EventoInsumo> {
    return this.http
      .post<ApiResponse<EventoInsumo>>(`${this.baseUrl}/${eventoId}/insumos`, request)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al agregar insumo');
        return response.data;
      }));
  }

  removeInsumo(eventoId: string, insumoId: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.baseUrl}/${eventoId}/insumos/${insumoId}`)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al eliminar insumo');
      }));
  }

  // ===================== ALIMENTACIÓN =====================

  getAlimentacion(eventoId: string): Observable<any[]> {
    return this.http
      .get<ApiResponse<any[]>>(`${this.baseUrl}/${eventoId}/alimentacion`)
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  // ===================== CATÁLOGOS =====================

  getEstados(): Observable<EstadoOption[]> {
    return this.http
      .get<ApiResponse<EstadoOption[]>>('/api/v1/catalogos/estado', {
        params: new HttpParams().set('contexto', 'evento'),
      })
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  getRolesEvento(): Observable<RolEventoOption[]> {
    return this.http
      .get<ApiResponse<RolEventoOption[]>>('/api/v1/catalogos/rol-evento')
      .pipe(map((response) => (response.success ? response.data : [])));
  }

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

  /** Obtiene proveedores vinculados a una Persona (para asignación en evento_personal) */
  getProveedoresOptions(): Observable<ProveedorOption[]> {
    return this.http
      .get<ApiResponse<any>>('/api/v1/proveedores', {
        params: new HttpParams().set('size', '500').set('tipo', 'persona'),
      })
      .pipe(map((response) => {
        if (!response.success) return [];
        const content = response.data.content ?? response.data;
        return content.map((p: any) => ({ id: p.id, nombre: p.nombre || p.especialidad || p.id }));
      }));
  }

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

  getInsumosOptions(): Observable<InsumoOption[]> {
    return this.http
      .get<ApiResponse<any>>('/api/v1/insumos', {
        params: new HttpParams().set('size', '500'),
      })
      .pipe(map((response) => {
        if (!response.success) return [];
        const content = response.data.content ?? response.data;
        return content.map((i: any) => ({ id: i.id, nombre: i.nombre }));
      }));
  }
}
