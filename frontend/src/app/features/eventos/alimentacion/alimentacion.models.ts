/**
 * Modelos del módulo de Alimentación por Evento.
 */

export interface Alimentacion {
  id: string;
  eventoId: string;
  descripcion: string;
  cantidad: number;
  tipoMovimiento: string;
  fecha: string;
  createdBy: string;
}

export interface CreateAlimentacionRequest {
  cantidad: number;
  descripcion?: string;
}
