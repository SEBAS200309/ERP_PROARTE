/**
 * Modelos del módulo de Presentaciones.
 */

export interface Presentacion {
  id: string;
  titulo: string;
  descripcion: string;
  servicioId: string | null;
  servicioNombre: string | null;
  contenido: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface CreatePresentacionRequest {
  titulo: string;
  descripcion?: string;
  servicioId?: string;
  contenido?: string;
}

export interface UpdatePresentacionRequest {
  titulo?: string;
  descripcion?: string;
  servicioId?: string;
  contenido?: string;
}

export interface ServicioOption {
  id: string;
  nombre: string;
}
