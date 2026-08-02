/**
 * Modelos del módulo de Órdenes de Compra.
 */

export interface OrdenCompra {
  id: string;
  codigo: string;
  solicitudId: string;
  descripcion: string;
  monto: number;
  estadoId: string;
  createdBy: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOrdenCompraRequest {
  codigo?: string;
  solicitudId: string;
  descripcion?: string;
  monto?: number;
  estadoId: string;
}

export interface UpdateOrdenCompraRequest {
  solicitudId?: string;
  descripcion?: string;
  monto?: number;
  estadoId?: string;
}

export interface EstadoOption {
  id: string;
  nombre: string;
}

export interface SolicitudOption {
  id: string;
  nombre: string;
}
