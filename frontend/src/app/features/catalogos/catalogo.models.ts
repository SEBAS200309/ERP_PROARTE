/**
 * Modelos del módulo de Catálogos.
 */

/** Tipos válidos de catálogo */
export type TipoCatalogo =
  | 'tipo-documento'
  | 'rol-entidad'
  | 'estado'
  | 'categoria-servicio'
  | 'unidad-medida'
  | 'rol-evento';

/** Metadatos de cada tipo de catálogo */
export interface TipoCatalogoInfo {
  tipo: TipoCatalogo;
  label: string;
  description: string;
}

/** Catálogo de tipos disponibles */
export const TIPOS_CATALOGO: TipoCatalogoInfo[] = [
  { tipo: 'tipo-documento', label: 'Tipo de Documento', description: 'Cédula, NIT, pasaporte, etc.' },
  { tipo: 'rol-entidad', label: 'Rol de Entidad', description: 'Cliente, proveedor, aliado, etc.' },
  { tipo: 'estado', label: 'Estado', description: 'Estados de cotizaciones, eventos, etc.' },
  { tipo: 'categoria-servicio', label: 'Categoría de Servicio', description: 'Categorías para servicios ofrecidos' },
  { tipo: 'unidad-medida', label: 'Unidad de Medida', description: 'Horas, unidades, metros, etc.' },
  { tipo: 'rol-evento', label: 'Rol de Evento', description: 'Roles de participación en eventos' },
];

/** Modelo de un valor de catálogo */
export interface CatalogoItem {
  id: string;
  nombre: string;
  contexto?: string;
  abreviatura?: string;
  activo: boolean;
  createdAt: string;
  updatedAt: string;
}

/** DTO para crear un valor de catálogo */
export interface CreateCatalogoRequest {
  nombre: string;
  contexto?: string;
  abreviatura?: string;
}

/** DTO para actualizar un valor de catálogo */
export interface UpdateCatalogoRequest {
  nombre: string;
  contexto?: string;
  abreviatura?: string;
}
