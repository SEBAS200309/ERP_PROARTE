/**
 * Modelos del módulo de Cotizaciones.
 */

export interface Cotizacion {
  id: string;
  codigo: string;
  estadoId: string;
  fechaVencimiento: string | null;
  total: number;
  personaId: string | null;
  empresaId: string | null;
  createdBy: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
  items: CotizacionItem[];
}

export interface CotizacionItem {
  id: string;
  cotizacionId: string;
  servicioId: string;
  cantidad: number;
  precioUnitario: number;
  descuentoRecargoId: string | null;
  subtotal: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateCotizacionRequest {
  codigo?: string;
  estadoId: string;
  fechaVencimiento?: string | null;
  personaId?: string | null;
  empresaId?: string | null;
  items?: CotizacionItemRequest[];
}

export interface UpdateCotizacionRequest {
  estadoId?: string;
  fechaVencimiento?: string | null;
  personaId?: string | null;
  empresaId?: string | null;
  items?: CotizacionItemRequest[];
}

export interface CotizacionItemRequest {
  servicioId: string;
  cantidad: number;
  precioUnitario: number;
  descuentoRecargoId?: string | null;
}

export interface CambiarEstadoRequest {
  estadoId: string;
}

export interface EstadoOption {
  id: string;
  nombre: string;
}

export interface PersonaOption {
  id: string;
  nombre: string;
}

export interface EmpresaOption {
  id: string;
  nombre: string;
}

export interface ServicioOption {
  id: string;
  nombre: string;
}

export interface DescuentoRecargoOption {
  id: string;
  nombre: string;
  valor: number;
}
