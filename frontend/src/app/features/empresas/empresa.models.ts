/**
 * Modelos del módulo de Empresas.
 */

export interface Empresa {
  id: string;
  razonSocial: string;
  nit: string | null;
  direccion: string | null;
  telefono: string | null;
  email: string | null;
  rolEntidadId: string | null;
  activo: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEmpresaRequest {
  razonSocial: string;
  nit?: string | null;
  direccion?: string | null;
  telefono?: string | null;
  email?: string | null;
  rolEntidadId?: string | null;
}

export interface UpdateEmpresaRequest {
  razonSocial?: string;
  nit?: string | null;
  direccion?: string | null;
  telefono?: string | null;
  email?: string | null;
  rolEntidadId?: string | null;
}

export interface CatalogoOption {
  id: string;
  nombre: string;
}

export interface PersonaAsociada {
  id: string;
  nombres: string;
  apellidos: string;
  documento: string | null;
  email: string | null;
  telefono: string | null;
}
