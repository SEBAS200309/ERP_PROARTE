/**
 * Modelos del módulo de Roles.
 */
export interface Rol {
    id: string;
    nombre: string;
    descripcion: string;
    configuracion?: Record<string, Record<string, boolean>>;
    activo: boolean;
    createdAt: string;
    updatedAt: string;
}

export interface CreateRolRequest {
    nombre: string;
    descripcion: string;
    configuracion?: Record<string, Record<string, boolean>>;
}

export interface UpdateRolRequest {
    nombre?: string;
    descripcion?: string;
    configuracion?: Record<string, Record<string, boolean>>;
}