/**
 * Modelos del módulo de Proveedores.
 */

export interface Proveedor {
  id: string;
  personaId: string | null;
  empresaId: string | null;
  especialidad: string | null;
  activo: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProveedorRequest {
  personaId?: string | null;
  empresaId?: string | null;
  especialidad?: string | null;
}

export interface UpdateProveedorRequest {
  personaId?: string | null;
  empresaId?: string | null;
  especialidad?: string | null;
}

export interface PortafolioItem {
  id: string;
  proveedorId: string;
  servicioId: string;
  precioUnitario: number;
  activo: boolean;
}

export interface CreatePortafolioRequest {
  servicioId: string;
  precioUnitario: number;
}

export interface UpdatePortafolioRequest {
  servicioId?: string;
  precioUnitario?: number;
}

export interface SolicitudServicio {
  id: string;
  proveedorId: string;
  servicioId: string;
  eventoId: string | null;
  estadoId: string | null;
  descripcion: string | null;
  activo: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSolicitudRequest {
  proveedorId: string;
  servicioId: string;
  eventoId?: string | null;
  estadoId?: string | null;
  descripcion?: string | null;
}

export interface UpdateSolicitudRequest {
  proveedorId?: string;
  servicioId?: string;
  eventoId?: string | null;
  estadoId?: string | null;
  descripcion?: string | null;
}

export interface CatalogoOption {
  id: string;
  nombre: string;
}

export interface PersonaOption {
  id: string;
  nombres: string;
  apellidos: string;
}

export interface EmpresaOption {
  id: string;
  razonSocial: string;
}

export interface ServicioOption {
  id: string;
  nombre: string;
}

export interface EventoOption {
  id: string;
  nombre: string;
}
