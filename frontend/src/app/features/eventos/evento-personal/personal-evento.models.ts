/**
 * Modelos para Personal de Evento.
 */

export interface EventoPersonal {
  id: string;
  eventoId: string;
  personaId: string;
  proveedorId: string;
  servicioId: string | null;
  valorTurno: number | null;
  tieneArl: boolean;
  tieneOp: boolean;
  observaciones: string | null;
  alertaArl: string | null;
  alertaOp: string | null;
}

export interface CreateEventoPersonalRequest {
  personaId: string;
  proveedorId: string;
  servicioId?: string | null;
  tieneArl?: boolean;
  tieneOp?: boolean;
  observaciones?: string | null;
}

export interface UpdateEventoPersonalRequest {
  servicioId?: string | null;
  tieneArl?: boolean;
  tieneOp?: boolean;
  observaciones?: string | null;
}

export interface CalcularTurnoResult {
  valor_turno: number;
}
