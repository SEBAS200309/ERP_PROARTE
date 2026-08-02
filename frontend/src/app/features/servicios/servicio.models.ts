/**
 * Modelos del módulo de Servicios y Descuentos/Recargos.
 */

export interface Servicio {
  id: string;
  nombre: string;
  descripcion: string | null;
  esPropio: boolean;
  requiereOc: boolean;
  servicioPadreId: string | null;
  categoriaId: string | null;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateServicioRequest {
  nombre: string;
  descripcion?: string | null;
  esPropio?: boolean;
  requiereOc?: boolean;
  servicioPadreId?: string | null;
  categoriaId?: string | null;
}

export interface UpdateServicioRequest {
  nombre?: string;
  descripcion?: string | null;
  esPropio?: boolean;
  requiereOc?: boolean;
  servicioPadreId?: string | null;
  categoriaId?: string | null;
}

export interface DescuentoRecargo {
  id: string;
  nombre: string;
  valor: number;
  tipoId: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDescuentoRecargoRequest {
  nombre: string;
  valor: number;
  tipoId: string;
}

export interface UpdateDescuentoRecargoRequest {
  nombre?: string;
  valor?: number;
  tipoId?: string;
}

export interface AplicarDescuentoRequest {
  descuentoRecargoId: string;
  servicioId?: string | null;
  personaId?: string | null;
  empresaId?: string | null;
}

export interface CategoriaOption {
  id: string;
  nombre: string;
}

export interface ServicioTreeNode {
  servicio: Servicio;
  children: ServicioTreeNode[];
  expanded: boolean;
}
