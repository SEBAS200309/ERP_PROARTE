/**
 * Modelos del módulo de Personas.
 */

export interface Persona {
  id: string;
  nombres: string;
  apellidos: string;
  tipoDocumentoId: string | null;
  documento: string | null;
  telefono: string | null;
  email: string | null;
  direccion: string | null;
  rolEntidadId: string | null;
  activo: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePersonaRequest {
  nombres: string;
  apellidos: string;
  tipoDocumentoId?: string | null;
  documento?: string | null;
  telefono?: string | null;
  email?: string | null;
  direccion?: string | null;
  rolEntidadId?: string | null;
}

export interface UpdatePersonaRequest {
  nombres?: string;
  apellidos?: string;
  tipoDocumentoId?: string | null;
  documento?: string | null;
  telefono?: string | null;
  email?: string | null;
  direccion?: string | null;
  rolEntidadId?: string | null;
}

export interface CatalogoOption {
  id: string;
  nombre: string;
}

export interface EmpresaAsociada {
  id: string;
  empresaId: string;
  razonSocial: string;
  cargo: string;
}

export interface AsociarEmpresaRequest {
  empresaId: string;
  cargo?: string;
}
