/**
 * Modelos del módulo de Mensajes.
 */

export interface Mensaje {
  id: string;
  nombre: string;
  contenido: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface CreateMensajeRequest {
  nombre: string;
  contenido?: string;
}

export interface UpdateMensajeRequest {
  nombre?: string;
  contenido?: string;
}
