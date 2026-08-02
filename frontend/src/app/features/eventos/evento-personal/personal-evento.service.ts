import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { ApiResponse } from '../../../core/models/auth.models';
import {
  EventoPersonal,
  CreateEventoPersonalRequest,
  UpdateEventoPersonalRequest,
  CalcularTurnoResult,
} from './personal-evento.models';

/**
 * Servicio para gestionar el personal asignado a un evento.
 * No extiende BaseCrudService porque el baseUrl es dinámico por evento.
 */
@Injectable({ providedIn: 'root' })
export class PersonalEventoService {
  private readonly http = inject(HttpClient);

  private buildUrl(eventoId: string): string {
    return `/api/v1/eventos/${eventoId}/personal`;
  }

  getAll(eventoId: string): Observable<EventoPersonal[]> {
    return this.http
      .get<ApiResponse<EventoPersonal[]>>(this.buildUrl(eventoId))
      .pipe(map((response) => (response.success ? response.data : [])));
  }

  create(eventoId: string, request: CreateEventoPersonalRequest): Observable<EventoPersonal> {
    return this.http
      .post<ApiResponse<EventoPersonal>>(this.buildUrl(eventoId), request)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al agregar personal');
        return response.data;
      }));
  }

  update(eventoId: string, personalId: string, request: UpdateEventoPersonalRequest): Observable<EventoPersonal> {
    return this.http
      .put<ApiResponse<EventoPersonal>>(`${this.buildUrl(eventoId)}/${personalId}`, request)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al actualizar personal');
        return response.data;
      }));
  }

  delete(eventoId: string, personalId: string): Observable<void> {
    return this.http
      .delete<ApiResponse<void>>(`${this.buildUrl(eventoId)}/${personalId}`)
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al eliminar personal');
      }));
  }

  calcularValorTurno(eventoId: string, eventoPersonalId: string): Observable<CalcularTurnoResult> {
    return this.http
      .post<ApiResponse<CalcularTurnoResult>>(
        `${this.buildUrl(eventoId)}/execute/calcular_turno`,
        { params: { evento_personal_id: eventoPersonalId } }
      )
      .pipe(map((response) => {
        if (!response.success) throw new Error(response.message || 'Error al calcular valor del turno');
        return response.data;
      }));
  }
}
