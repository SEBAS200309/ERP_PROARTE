/**
 * Modelos del módulo de Leads.
 */

export interface Lead {
  id: string;
  descripcion: string;
  estadoId: string;
  personaId: string | null;
  empresaId: string | null;
  createdBy: string;
  activo: boolean;
  createdAt: string;
}

export interface CreateLeadRequest {
  descripcion: string;
  estadoId: string;
  personaId?: string | null;
  empresaId?: string | null;
}

export interface UpdateLeadRequest {
  descripcion?: string;
  estadoId?: string;
  personaId?: string | null;
  empresaId?: string | null;
}

export interface EstadoCatalogo {
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
