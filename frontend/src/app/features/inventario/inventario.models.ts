/**
 * Modelos del módulo de Inventario.
 */

export interface Insumo {
  id: string;
  nombre: string;
  descripcion: string;
  unidadMedidaId: string;
  stockActual: number;
  createdAt: string;
  updatedAt: string;
}

export interface Movimiento {
  id: string;
  insumoId: string;
  tipoMovimiento: string;
  cantidad: number;
  fecha: string;
  motivo: string;
  createdBy: string;
}

export interface CreateMovimientoRequest {
  insumoId: string;
  cantidad: number;
  motivo?: string;
}

export interface InsumoOption {
  id: string;
  nombre: string;
  stockActual: number;
}
