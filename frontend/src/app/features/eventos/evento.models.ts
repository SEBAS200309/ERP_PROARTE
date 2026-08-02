/**
 * Modelos del módulo de Eventos.
 */

export interface Evento {
  id: string;
  cotizacionId: string | null;
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
  lugar: string;
  estadoId: string;
  activo: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEventoRequest {
  cotizacionId?: string;
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
  lugar: string;
  estadoId: string;
}

export interface UpdateEventoRequest {
  nombre?: string;
  fechaInicio?: string;
  fechaFin?: string;
  lugar?: string;
  estadoId?: string;
}

export interface CrearDesdeCotizacionRequest {
  cotizacionId: string;
}

// === Contactos / Personas ===

export interface EventoContacto {
  id: string;
  eventoId: string;
  personaId: string;
  rolEventoId: string;
  observaciones: string | null;
}

export interface EventoContactoRequest {
  personaId: string;
  rolEventoId: string;
  observaciones?: string | null;
}

// === Proveedores ===

export interface EventoProveedor {
  id: string;
  eventoId: string;
  proveedorId: string;
  servicioId: string;
}

export interface EventoProveedorRequest {
  proveedorId: string;
  servicioId: string;
}

// === Observaciones ===

export interface Observacion {
  id: string;
  eventoId: string;
  texto: string;
  fecha: string;
  createdBy: string;
}

export interface ObservacionRequest {
  texto: string;
}

// === Insumos ===

export interface EventoInsumo {
  id: string;
  eventoId: string;
  insumoId: string;
  cantidad: number;
}

export interface EventoInsumoRequest {
  insumoId: string;
  cantidad: number;
}

// === Alimentación ===

export interface EventoAlimentacion {
  id: string;
  eventoId: string;
  [key: string]: any;
}

// === Catálogos ===

export interface EstadoOption {
  id: string;
  nombre: string;
}

export interface RolEventoOption {
  id: string;
  nombre: string;
}

export interface PersonaOption {
  id: string;
  nombre: string;
}

export interface ProveedorOption {
  id: string;
  nombre: string;
}

export interface ServicioOption {
  id: string;
  nombre: string;
}

export interface InsumoOption {
  id: string;
  nombre: string;
}
